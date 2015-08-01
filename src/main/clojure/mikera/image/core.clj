(ns mikera.image.core
  (:require [clojure.java.io :refer [file resource]])
  (:require [clojure.string :refer [lower-case split]])
  (:require [mikera.image.colours :as col])
  (:require [mikera.image.filters :as filt])
  (:require [mikera.image.protocols :as protos])
  (:use mikera.cljutils.error)
  (:import [java.awt Graphics2D])
  (:import [java.awt.image BufferedImage BufferedImageOp])
  (:import [javax.imageio ImageIO IIOImage ImageWriter ImageWriteParam])
  (:import [org.imgscalr Scalr])
  (:import [mikera.gui Frames]))

(set! *unchecked-math* true)
(set! *warn-on-reflection* true)

(defn new-image
  "Creates a new BufferedImage with the specified width and height.
   Uses BufferedImage/TYPE_INT_ARGB format by default,
   but also supports BufferedImage/TYPE_INT_RGB when alpha channel is not needed."
  (^java.awt.image.BufferedImage [width height]
    (new-image width height true))
  (^java.awt.image.BufferedImage [width height alpha?]
    (if alpha?
      (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB)
      (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_RGB))))

(defn copy 
  "Copies an image to a new BufferedImage"
  ([^BufferedImage src]
    (let [w (width src)
          h (height src)
          dst (new-image w h)]
      (.drawImage (.getGraphics dst) src (int 0) (int 0) nil)
      dst)))

(defn resize
  "Resizes an image to the specified width and height. If height is omitted,
   maintains the aspect ratio."
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil))
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image new-width]
    (resize image new-width (/ (* new-width (.getHeight image)) (.getWidth image)))))

(defn scale-image
  "DEPRECATED: use 'resize' instead"
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil)))

(defn scale
  "Scales an image by a given factor or ratio."
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image factor]
    (resize image (* (.getWidth image) factor) (* (.getHeight image) factor)))
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image width-factor height-factor]
    (resize image (* (.getWidth image) width-factor) (* (.getHeight image) height-factor))))

(defn ensure-default-image-type
  "If the provided image is does not have the default image type
  (BufferedImage/TYPE_INT_ARGB) a copy with that type is returned."
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image]
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
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image factor]
    (scale image factor)))

(defn flip
  "Flips an image in the specified direction :horizontal or :vertical"
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image direction]
   (cond
     (= :horizontal direction)
     (Scalr/rotate image org.imgscalr.Scalr$Rotation/FLIP_HORZ nil)
     (= :vertical direction)
     (Scalr/rotate image org.imgscalr.Scalr$Rotation/FLIP_VERT nil)
     :else (error "Flip direction not valid: " direction))))

(defn rotate
  "Rotate an image clockwise by x degrees"
  (^java.awt.image.BufferedImage [^java.awt.image.BufferedImage image degrees]
   (let [rot (mod degrees 360)]
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
  (^ints [^java.awt.image.BufferedImage image]
    (.getDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn set-pixels
  "Sets the pixels in a BufferedImage using a primitive int[] array.
   This is often an efficient format for manipulating an image."
  ([^java.awt.image.BufferedImage image ^ints pixels]
    (.setDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) pixels)))

(defn get-pixel
  "Gets a single pixel in a BufferedImage."
  ^long [^java.awt.image.BufferedImage image ^long x ^long y]
  (.getRGB image x y))

(defn set-pixel
  "Sets a single pixel in a BufferedImage."
  [^java.awt.image.BufferedImage image ^long x ^long y ^long rgb]
  (.setRGB image x y rgb))

(defn width 
  "Gets the width of an image as a long value"
  (^long [^BufferedImage image]
    (.getWidth image)))

(defn height 
  "Gets the height of an image as a long value"
  (^long [^BufferedImage image]
    (.getHeight image)))

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

(defn show
  "Displays an image in a new frame.

   The frame includes simple menus for saving an image, and other handy utilities."
  ([image & {:keys [zoom title]}]
    (let [^java.awt.image.BufferedImage image (if zoom (mikera.image.core/zoom image (double zoom)) image)
          ^String title (or title "Imagez Frame")]
      (Frames/display image title))))

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

(defn save
  "Stores an image to disk.

  Accepts optional keyword arguments.

  `:quality` - decimal, between 0.0 and 1.0. Defaults to 0.8.

  `:progressive` - boolean, `true` turns progressive encoding on, `false`
  turns it off. Defaults to the default value in the ImageIO API -
  `ImageWriteParam/MODE_COPY_FROM_METADATA`. See
  [Java docs](http://docs.oracle.com/javase/7/docs/api/javax/imageio/ImageWriteParam.html).

  Examples:

    (save image \"/path/to/new/image.jpg\" :quality 1.0)
    (save image \"/path/to/new/image/jpg\" :progressive false)
    (save image \"/path/to/new/image/jpg\" :quality 0.7 :progressive true)

  Returns the path to the saved image when saved successfully."
  [^BufferedImage image path & {:keys [quality progressive]
                                  :or {quality 0.8
                                       progressive nil}}]
  (let [outfile (file path)
        ext (-> path (split #"\.") last lower-case)
        ^javax.imageio.ImageWriter writer (.next (ImageIO/getImageWritersByFormatName ext))
        ^javax.imageio.ImageWriteParam write-param (.getDefaultWriteParam writer)
        iioimage (IIOImage. image nil nil)
        outstream (ImageIO/createImageOutputStream outfile)]
    (apply-compression write-param quality ext)
    (apply-progressive write-param progressive)
    (doto writer
      (.setOutput outstream)
      (.write nil iioimage write-param)
      (.dispose))
    (.close outstream)
    path))
