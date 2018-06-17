(ns mikera.image.core
  "Main namespace for imagez image processing functionality"
  (:require [clojure.java.io :refer [file resource output-stream]])
  (:require [clojure.string :refer [lower-case split]])
  (:require [mikera.image.colours :as col])
  (:require [mikera.image.filters :as filt])
  (:require [mikera.image.protocols :as protos])
  (:use mikera.cljutils.error)
  (:import [java.awt Graphics2D Image Color])
  (:import [java.awt.image BufferedImage BufferedImageOp])
  (:import [javax.imageio ImageIO IIOImage ImageWriter ImageWriteParam])
  (:import [org.imgscalr Scalr]))

(set! *unchecked-math* :warn-on-boxed)
(set! *warn-on-reflection* true)

(declare width height)

(defn new-image
  "Creates a new BufferedImage with the specified width and height.
   
   Uses BufferedImage/TYPE_INT_ARGB format by default.

   Option type-or-alpha may be provided, which supports the following values:
    - Boolean: true gives BufferedImage/TYPE_INT_ARGB, false gives BufferedImage/TYPE_INT_RGB.
    - Integer: Specifies the exact image type, e.g. BufferedImage/TYPE_USHORT_GRAY
    - Any other value: Gives the default image type BufferedImage/TYPE_INT_ARGB

   Note that imagez assumes arguments are of BufferedImage/TYPE_INT_ARGB. Operations on other image
   types may not work correctly."
  (^java.awt.image.BufferedImage [width height]
    (new-image width height true))
  (^java.awt.image.BufferedImage [width height type-or-alpha?]
    (cond 
      (number? type-or-alpha?) (BufferedImage. (int width) (int height) (int type-or-alpha?))
      (false? type-or-alpha?) (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_RGB)
      :else (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB))))

(defn copy 
  "Copies an image to a new BufferedImage.

   May optionally specify the target image dimensions, or a resizing factor. Resizing during copy
   does *not* perform any interpolation: use scale instead if this is desired."
  (^java.awt.image.BufferedImage [^BufferedImage src]
    (let [w (width src)
          h (height src)
          dst (new-image w h)]
      (.drawImage (.getGraphics dst) src (int 0) (int 0) nil)
      dst))
  (^java.awt.image.BufferedImage [^BufferedImage src factor-or-shape]
      (if (number? factor-or-shape)
        (let [factor (double factor-or-shape)]
          (copy src (int (* (double (width src)) factor)) (int (* (double (height src)) factor))))
        (let [[w h] factor-or-shape]
          (copy src w h))))
  (^java.awt.image.BufferedImage [^BufferedImage src target-width target-height]
    (let [w (width src)
          h (height src)
          tw (long target-width)
          th (long target-height) 
          dst (new-image tw th)]
      (.drawImage (.getGraphics dst) src 
        (int 0) (int 0) (int tw) (int th) ;; dest coordinates
        (int 0) (int 0) (int w) (int h)          ;; source co-ordinates
        nil)
      dst)))

(defn resize
  "Resizes an image to the specified width and height. If height is omitted,
   maintains the aspect ratio."
  (^java.awt.image.BufferedImage [^BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil))
  (^java.awt.image.BufferedImage [^BufferedImage image new-width]
    (resize image new-width (/ (* (long new-width) (.getHeight image)) (.getWidth image)))))

(defn scale-image
  "DEPRECATED: use 'resize' instead"
  (^java.awt.image.BufferedImage [^BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil)))

(defn scale
  "Scales an image by a given factor or ratio."
  (^java.awt.image.BufferedImage [^BufferedImage image factor]
    (resize image (* (.getWidth image) (double factor)) (* (.getHeight image) (double factor))))
  (^java.awt.image.BufferedImage [^BufferedImage image width-factor height-factor]
    (resize image (* (.getWidth image) (double width-factor)) (* (.getHeight image) (double height-factor)))))

(defn ensure-default-image-type
  "If the provided image is does not have the default image type
  (BufferedImage/TYPE_INT_ARGB) a copy with that type is returned."
  (^java.awt.image.BufferedImage [^BufferedImage image]
    (if (= BufferedImage/TYPE_INT_ARGB (.getType image))
      image
      (let [copy (new-image (.getWidth image) (.getHeight image))
            ^Graphics2D g (.getGraphics copy)]
        (.drawImage g image nil 0 0)
        copy))))

(defn load-image
  "Loads a BufferedImage from a string, file or a URL representing a resource
  on the classpath.

  Usage:

    (load-image \"/some/path/to/image.png\")
    ;; (require [clojure.java.io :refer [resource]])
    (load-image (resource \"some/path/to/image.png\"))"
  (^java.awt.image.BufferedImage [resource] (ensure-default-image-type (protos/as-image resource))))

(defn load-image-resource
  "Loads an image from a named resource on the classpath.

   Equivalent to (load-image (clojure.java.io/resource res-path))"
  (^java.awt.image.BufferedImage [res-path] (load-image (resource res-path))))

(defn zoom
  "Zooms into (scales) an image with a given scale factor."
  (^java.awt.image.BufferedImage [^BufferedImage image factor]
    (scale image factor)))

(defn flip
  "Flips an image in the specified direction :horizontal or :vertical"
  (^java.awt.image.BufferedImage [^BufferedImage image direction]
   (cond
     (= :horizontal direction)
     (Scalr/rotate image org.imgscalr.Scalr$Rotation/FLIP_HORZ nil)
     (= :vertical direction)
     (Scalr/rotate image org.imgscalr.Scalr$Rotation/FLIP_VERT nil)
     :else (error "Flip direction not valid: " direction))))

(defn rotate
  "Rotate an image clockwise by x degrees"
  (^java.awt.image.BufferedImage [^BufferedImage image degrees]
   (let [rot (double (mod degrees 360))]
     (cond
	     (== rot 0)
	       image
	     (== rot 90)
	       (Scalr/rotate image org.imgscalr.Scalr$Rotation/CW_90 nil)
	     (== rot 180)
	       (Scalr/rotate image org.imgscalr.Scalr$Rotation/CW_180 nil)
	     (== rot 270)
	       (Scalr/rotate image org.imgscalr.Scalr$Rotation/CW_270 nil)
	     :else (error "Rotation amount not valid: " degrees " current supported values must be a multiple of 90")))))

(defn get-pixels
  "Gets the pixels in a BufferedImage as a primitive int[] array.
   This is often an efficient format for manipulating an image."
  (^ints [^BufferedImage image]
    (.getDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn set-pixels
  "Sets the pixels in a BufferedImage using a primitive int[] array.
   This is often an efficient format for manipulating an image."
  ([^java.awt.image.BufferedImage image ^ints pixels]
    (.setDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) pixels)))

(defn get-pixel
  "Gets a single pixel in a BufferedImage."
  ^long [^BufferedImage image ^long x ^long y]
  (bit-and 0xFFFFFFFF (.getRGB image (int x) (int y))))

(defn set-pixel
  "Sets a single pixel in a BufferedImage."
  [^BufferedImage image ^long x ^long y ^long rgb]
  (.setRGB image (int x) (int y) (unchecked-int rgb)))

(defn width 
  "Gets the width of an image as a long value"
  (^long [^BufferedImage image]
    (.getWidth image)))

(defn height 
  "Gets the height of an image as a long value"
  (^long [^BufferedImage image]
    (.getHeight image)))

(defn graphics
  "Gets the Java Graphics2D object associated with an image"
  (^Graphics2D [image]
    (cond 
      (instance? Graphics2D image) image
      (instance? Image image) (.getGraphics ^Image image)
      :else (error "Can't get graphics for type: " (class image)))))

(defn fill-rect!
  "Fills a rectangle on the image with a specified ARGB value or Java Color. Mutates the image."
  ([image x y w h colour]
    (let [g (graphics image)
          ^Color colour (col/to-java-color colour)]
      (.setColor g colour)
      (.fillRect g (int x) (int y) (int w) (int h))
      image)))

(defn fill!
  "Fills the image with a specified ARGB value or Java Color. Mutates the image."
  ([image colour]
    (fill-rect! image 0 0 (width image) (height image) colour)))

(defn filter-image
  "Applies a filter to a source image.
  Filter may be either a BufferedImageOp or an Imagez filter.

   Returns a new image."
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image
    filter]
  (let [filter (filt/to-image-op filter)
        dest-img (.createCompatibleDestImage filter image (.getColorModel image))]
    (.filter filter image dest-img)
    dest-img)))

(defn sub-image
  "Gets a sub-image area from an image."
  (^java.awt.image.BufferedImage [^BufferedImage image x y w h]
    (.getSubimage image (int x) (int y) (int w) (int h))))

(defn gradient-image
  "Creates an image filled with a gradient according to the given spectrum function.
   Default is a filled gradient from left=0 to right=1."
  (^java.awt.image.BufferedImage [spectrum-fn w h]
    (let [w (int w)
          h (int h)
          im (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
          g (.getGraphics im)]
      (dotimes [i w]
        (.setColor g (col/color (spectrum-fn (/ (double i) w))))
        (.fillRect g (int i) (int 0) (int 1) (int h)))
      im))
  (^java.awt.image.BufferedImage [spectrum-fn]
    (gradient-image spectrum-fn 200 60)))

(defn- ^javax.imageio.ImageWriteParam apply-compression
  "Applies compression to the write parameter, if possible."
  [^javax.imageio.ImageWriteParam write-param quality ext]
  (cond (= ext "gif")
        (doto write-param
          (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
          (.setCompressionType "LZW"))

        (.canWriteCompressed write-param)
        (doto write-param
          (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
          (.setCompressionQuality quality)))
  write-param)

(defn- ^javax.imageio.ImageWriteParam apply-progressive
  "Applies progressive encoding, if possible.

  If `progressive-flag` is `true`, turns progressive encoding on, `false`
  turns it off. Defaults to `ImageWriteParam/MODE_COPY_FROM_METADATA`, which
  is the default in ImageIO API."
  [^javax.imageio.ImageWriteParam write-param progressive-flag]
  (when (.canWriteProgressive write-param)
    (let [mode-map {true  ImageWriteParam/MODE_DEFAULT
                    false ImageWriteParam/MODE_DISABLED}
          mode-flag (get mode-map
                         progressive-flag
                         ImageWriteParam/MODE_COPY_FROM_METADATA)]
      (doto write-param
        (.setProgressiveMode mode-flag))))
  write-param)


(defn write
  "Writes an image externally.

  `out` will be coerced to a `java.io.OutputStream` as per `clojure.java.io/output-stream`.
  `format-name` determines the format of the written file. See
  [ImageIO/getImageWritersByFormatName](https://docs.oracle.com/javase/7/docs/api/javax/imageio/ImageIO.html#getImageWritersByFormatName(java.lang.String))

  Accepts optional keyword arguments.

  `:quality` - decimal, between 0.0 and 1.0. Defaults to 0.8.

  `:progressive` - boolean, `true` turns progressive encoding on, `false`
  turns it off. Defaults to the default value in the ImageIO API -
  `ImageWriteParam/MODE_COPY_FROM_METADATA`. See
  [Java docs](http://docs.oracle.com/javase/7/docs/api/javax/imageio/ImageWriteParam.html).

  Examples:

    (write image (clojure.java.io/resource \"my/image.png\") \"png\" :quality 1.0)
    (write image my-output-stream \"jpg\" :progressive false)
    (write image \"/path/to/new/image/jpg\" \"jpg\" :quality 0.7 :progressive true)

  "
  [^java.awt.image.BufferedImage image out format-name & {:keys [quality progressive]
                                                         :or {quality 0.8
                                                              progressive nil}}]
  (let [^javax.imageio.ImageWriter writer (.next (ImageIO/getImageWritersByFormatName format-name))
        ^javax.imageio.ImageWriteParam write-param (.getDefaultWriteParam writer)
        iioimage (IIOImage. image nil nil)]
    (with-open [io-stream (output-stream out)
                outstream (ImageIO/createImageOutputStream io-stream)]
      (apply-compression write-param quality format-name)
      (apply-progressive write-param progressive)
      (doto writer
        (.setOutput outstream)
        (.write nil iioimage write-param)
        (.dispose)))))

(defn save
  "Stores an image to disk.

  See the documentation of `mikera.image.core/write` for optional arguments.

  Examples:

    (save image \"/path/to/new/image.jpg\" :quality 1.0)
    (save image \"/path/to/new/image/jpg\" :progressive false)
    (save image \"/path/to/new/image/jpg\" :quality 0.7 :progressive true)

  Returns the path to the saved image when saved successfully."
  [^java.awt.image.BufferedImage image path & {:keys [quality progressive]
                                               :or {quality 0.8
                                                    progressive nil}
                                               :as opts}]
  (let [outfile (file path)
        ext (-> path (split #"\.") last lower-case)]
    (apply write image outfile ext opts)
    path))

(comment ;; some quick functions for testing
  (def test-image (let [img (new-image 2 2)]
                    (fill! img col/black)
                    (set-pixel img 0 0 col/clear)
                    (set-pixel img 1 1 col/green)
                    img))
  
  (show (copy test-image 100 100))
  )
