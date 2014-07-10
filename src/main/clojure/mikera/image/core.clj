(ns mikera.image.core
  (:require [clojure.java.io :refer [file resource]])
  (:require [clojure.string :refer [lower-case split]])
  (:require [mikera.image.colours :as col])
  (:require [mikera.image.filters :as filt])
  (:require [mikera.image.protocols :as protos])
  (:use mikera.cljutils.error) 
  (:import [java.awt.image BufferedImage BufferedImageOp])
  (:import [javax.imageio ImageIO IIOImage ImageWriter ImageWriteParam])
  (:import [org.imgscalr Scalr])
  (:import [mikera.gui Frames]))

(set! *unchecked-math* true)
(set! *warn-on-reflection* true)

(defn new-image
  "Creates a new BufferedImage with the specified width and height.
   Uses ARGB format by default."
  (^BufferedImage [width height]
    (new-image width height true))
  (^BufferedImage [width height alpha?]
    (if alpha?
      (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB)
      (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_RGB))))

(defn resize
  "Resizes an image to the specified width and height. If height is omitted,
  maintains the aspect ratio."
  (^BufferedImage [^BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil))
  (^BufferedImage [^BufferedImage image new-width]
    (resize new-width (/ (* new-width (.getHeight image)) (.getWidth image)))))

(defn scale-image
  "DEPRECATED: use 'resize' instead"
  (^BufferedImage [^BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil)))

(defn scale
  "Scales an image by a given factor or ratio."
  (^BufferedImage [^BufferedImage image factor]
    (resize image (* (.getWidth image) factor) (* (.getHeight image) factor)))
  (^BufferedImage [^BufferedImage image width-factor height-factor]
    (resize image (* (.getWidth image) width-factor) (* (.getHeight image) height-factor))))

(defn load-image
  "Loads a BufferedImage from a string, file or a URL representing a resource
  on the classpath.

  Usage:

    (load-image \"/some/path/to/image.png\")
    ;; (require [clojure.java.io :refer [resource]])
    (load-image (resource \"some/path/to/image.png\"))"
  (^BufferedImage [resource] (protos/as-image resource)))

(defn load-image-resource
  "Loads an image from a named resource on the classpath.

   Equivalent to (load-image (clojure.java.io/resource res-path))"
  (^BufferedImage [res-path] (load-image (resource res-path))))

(defn zoom
  "Zooms into (scales) an image with a given scale factor."
  (^BufferedImage [^BufferedImage image factor]
    (scale image factor)))

(defn flip [^BufferedImage image direction]
  "Flips an image in the specified direction :horizontal or :vertical"
  (cond
    (= :horizontal direction) (TODO)
    (= :vertical direction) (TODO)
    :else (error "Flip direction not valid: " direction))) 

(defn get-pixels
  "Gets the pixels in a BufferedImage as a primitive int[] array.
   This is often an efficient format for manipulating an image."
  (^ints [^BufferedImage image]
    (.getDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn set-pixels
  "Sets the pixels in a BufferedImage using a primitive int[] array.
   This is often an efficient format for manipulating an image."
  ([^BufferedImage image ^ints pixels]
    (.setDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) pixels)))

(defn filter-image
  "Applies a filter to a source image.
  Filter may be either a BufferedImageOp or an Imagez filter.

   Returns a new image."
  (^BufferedImage [^java.awt.image.BufferedImage image
                   filter]
  (let [filter (filt/to-image-op filter)
        dest-img (.createCompatibleDestImage filter image (.getColorModel image))]
    (.filter filter image dest-img)
    dest-img)))

(defn sub-image
  "Gets a sub-image area from an image."
  (^BufferedImage [^BufferedImage image x y w h]
    (.getSubimage image (int x) (int y) (int w) (int h))))

(defn gradient-image
  "Creates an image filled with a gradient according to the given spectrum function.
   Default is a filled gradient from left=0 to right=1."
  (^BufferedImage [spectrum-fn w h]
    (let [w (int w)
          h (int h)
          im (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
          g (.getGraphics im)]
      (dotimes [i w]
        (.setColor g (col/color (spectrum-fn (/ (double i) w))))
        (.fillRect g (int i) (int 0) (int 1) (int h)))
      im))
  (^BufferedImage [spectrum-fn]
    (gradient-image spectrum-fn 200 60)))

(defn show
  "Displays an image in a new frame.

   The frame includes simple menus for saving an image, and other handy utilities."
  ([image & {:keys [zoom title]}]
    (let [^BufferedImage image (if zoom (mikera.image.core/zoom image (double zoom)) image)
          ^String title (or title "Imagez Frame")]
      (Frames/display image title))))

(defn- ^ImageWriteParam apply-compression
  "Applies compression to the write parameter, if possible."
  [^ImageWriteParam write-param quality]
  (when (.canWriteCompressed write-param)
    (doto write-param
      (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
      (.setCompressionQuality quality)))
  write-param)

(defn- ^ImageWriteParam apply-progressive
  "Applies progressive encoding, if possible.

  If `progressive-flag` is `true`, turns progressive encoding on, `false`
  turns it off. Defaults to `ImageWriteParam/MODE_COPY_FROM_METADATA`, which
  is the default in ImageIO API."
  [^ImageWriteParam write-param progressive-flag]
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
        ^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName ext))
        ^ImageWriteParam write-param (.getDefaultWriteParam writer)
        iioimage (IIOImage. image nil nil)
        outstream (ImageIO/createImageOutputStream outfile)]
    (apply-compression write-param quality)
    (apply-progressive write-param progressive)
    (doto writer
      (.setOutput outstream)
      (.write nil iioimage write-param)
      (.dispose))
    (.close outstream)
    path))
