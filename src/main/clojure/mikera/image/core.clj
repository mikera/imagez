(ns mikera.image.core
  (:import [java.awt.image BufferedImage])
  (:import [org.imgscalr Scalr]))

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

(defn zoom [factor ^BufferedImage image]
  (scale-image image 
               (int (* (.getWidth image) factor))
               (int (* (.getHeight image) factor))))