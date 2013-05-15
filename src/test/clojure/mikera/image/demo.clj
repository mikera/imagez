(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters))

(def ant (load-image "mikera/image/samples/Ant.png"))

(defn demo []

(show ant)

;; demo of various filters
(show (filter-image (grayscale) ant))
(show (filter-image (box-blur 2 2) ant))
(show (filter-image (contrast 0.5) ant))
(show (filter-image (brightness 2.0) ant))
(show (filter-image (invert) ant))

)

