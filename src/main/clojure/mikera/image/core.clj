(ns mikera.image.core
  (:import [java.awt.image BufferedImage]))

(set! *unchecked-math* true)
(set! *warn-on-reflection* true)

(defn new-image
  "Creates a new BufferedImage with the specified width and height. 
   Uses ARGB format by default."
  [^long width ^long height]
  (BufferedImage. (int width) (int height) BufferedImage/TYPE_INT_ARGB))