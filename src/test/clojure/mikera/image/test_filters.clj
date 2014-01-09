(ns mikera.image.test-filters
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters)
  (:use clojure.test)
  (:import [java.awt.image BufferedImage BufferedImageOp]))

(deftest test-filter
  (let [^BufferedImage bi (new-image 5 5)
        ^BufferedImage fi (filter-image bi (box-blur))]
    (is (not (identical? bi fi)))
    (is (== 5 (.getWidth fi)))))

(deftest test-filter-apply
  (let [^BufferedImage bi (new-image 5 5)
        ^BufferedImage fi ((box-blur) bi )]
    (is (not (identical? bi fi)))
    (is (== 5 (.getWidth fi))))) 
