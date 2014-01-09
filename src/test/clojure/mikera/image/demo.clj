(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters))

(def ant (load-image "mikera/image/samples/Ant.png"))

(defn demo []

(show ant)

;; demo of various filters used in functional style
(show ((grayscale) ant))
(show ((box-blur 2 2) ant))
(show ((contrast 0.5) ant))
(show ((brightness 2.0) ant))
(show ((invert) ant))

)

