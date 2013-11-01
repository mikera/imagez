(ns mikera.image.core
  (:import [java.awt.image BufferedImage]
           [org.imgscalr Scalr]
           [mikera.gui Frames])
  (:require [clojure.java.io :as io]))

(set! *unchecked-math* true)
(set! *warn-on-reflection* true)

(defn new-image
  "Creates a new BufferedImage with the specified width and height. 
   Uses ARGB format by default."
  [^long width ^long height]
  (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB))

(defn scale-image
  [^BufferedImage image new-width new-height]
  (Scalr/resize image 
                org.imgscalr.Scalr$Method/BALANCED 
                org.imgscalr.Scalr$Mode/FIT_EXACT 
                (int new-width) (int new-height) nil))

(defn load-image 
  "Loads a BufferedImage from an url."
  (^java.awt.image.BufferedImage [url]
    (javax.imageio.ImageIO/read url)))

(defn load-resource-image 
  "Loads a BufferedImage from a resource on the classpath.

   Usage: (load-image \"some/path/image-name.png\")"
  (^java.awt.image.BufferedImage [resource-name]
    (load-image (io/resource resource-name))))

(defn zoom 
  "Zooms into (scales) an image with a given scale factor."
  ([factor ^BufferedImage image]
    (scale-image image 
                 (int (* (.getWidth image) factor))
                 (int (* (.getHeight image) factor)))))

(defn get-pixels 
  "Gets the pixels in a BufferedImage as a primitive array.
   This is probably the fastest format for manipulating an image."
  ([^BufferedImage image]
    (.getDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn set-pixels 
  "Sets the pixels in a BufferedImage using a primitive array.
   This is probably the fastest format for manipulating an image."
  ([^BufferedImage image ^ints pixels]
    (.setDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) pixels)))

(defn filter-image 
  "Applies a BufferedImageOp filter to a source image.
   Returns a new image."
  (^BufferedImage [filter image]
  (let [^java.awt.image.BufferedImageOp filter filter
        ^java.awt.image.BufferedImage image image 
        dest-img (.createCompatibleDestImage filter image (.getColorModel image))]
    (.filter filter image dest-img)
    dest-img)))

(defn show
  "Displays an image in a new JFrame"
  ([image & {:keys [zoom title]}]
    (let [^BufferedImage image (if zoom (mikera.image.core/zoom (double zoom) image) image)
          ^String title (or title "Imagez Frame")]
      (Frames/display image title))))
