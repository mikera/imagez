(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters)
  (:require [clojure.java.io :refer [resource]]))

(def ant (-> "mikera/image/samples/Ant.png" resource load-image))

(defn demo []

(show ant)

;; demo of various filters used in functional style
(show ((grayscale) ant))
(show ((box-blur 2 2) ant))
(show ((contrast 0.5) ant))
(show ((brightness 2.0) ant))
(show ((invert) ant))

)
