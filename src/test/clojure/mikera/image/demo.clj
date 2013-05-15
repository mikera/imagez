(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters))

(def ant (load-image "mikera/image/samples/Ant.png"))

(defn demo []

(show ant)

;; demo of various filters
(show (filter-image (grayscale) ant))
(show (filter-image (box-blur 3 3) ant))

)

