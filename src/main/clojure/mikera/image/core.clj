(ns mikera.image.core
  (:require [mikera.image.colours :as col])
  (:import [java.awt.image BufferedImage])
  (:import [org.imgscalr Scalr])
  (:import [mikera.gui Frames]))

(set! *unchecked-math* true)
(set! *warn-on-reflection* true)

(defn new-image
  "Creates a new BufferedImage with the specified width and height.
   Uses ARGB format by default."
  (^BufferedImage [width height]
    (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB)))

(defn scale-image
  "Scales an image to a specified width and height"
  (^BufferedImage [^BufferedImage image new-width new-height]
    (Scalr/resize image
                  org.imgscalr.Scalr$Method/BALANCED
                  org.imgscalr.Scalr$Mode/FIT_EXACT
                  (int new-width) (int new-height) nil)))

(defn- ^ClassLoader context-class-loader []
  (.getContextClassLoader (Thread/currentThread)))

(defn load-image
  "Loads a BufferedImage from a resource on the classpath.

   Usage: (load-image \"some/path/image-name.png\")"
  (^BufferedImage [resource-name]
    (javax.imageio.ImageIO/read (.getResource (context-class-loader) resource-name))))

(defn zoom
  "Zooms into (scales) an image with a given scale factor."
  (^BufferedImage [^BufferedImage image factor]
    (scale-image image
                 (int (* (.getWidth image) factor))
                 (int (* (.getHeight image) factor)))))

(defn get-pixels
  "Gets the pixels in a BufferedImage as a primitive array.
   This is probably the fastest format for manipulating an image."
  (^ints [^BufferedImage image]
    (.getDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) nil)))

(defn set-pixels
  "Sets the pixels in a BufferedImage using a primitive array.
   This is probably the fastest format for manipulating an image."
  ([^BufferedImage image ^ints pixels]
    (.setDataElements (.getRaster image) 0 0 (.getWidth image) (.getHeight image) pixels)))

(defn filter-image
  "Applies a BufferedImageOp filter to a source image.
   Returns a new image."
  (^BufferedImage [^java.awt.image.BufferedImage image
                   ^java.awt.image.BufferedImageOp filter]
  (let [dest-img (.createCompatibleDestImage filter image (.getColorModel image))]
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
  "Displays an image in a new JFrame"
  ([image & {:keys [zoom title]}]
    (let [^BufferedImage image (if zoom (mikera.image.core/zoom (double zoom) image) image)
          ^String title (or title "Imagez Frame")]
      (Frames/display image title))))
