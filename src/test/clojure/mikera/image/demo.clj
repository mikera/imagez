(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters))

(def ant (load-image "mikera/image/samples/Ant.png"))

(show ant)

(show (filter-image (grayscale) ant))


