(ns mikera.image.test-core
  (:use mikera.image.core)
  (:use clojure.test)
  (:import [java.awt.image BufferedImage]))

(deftest test-new-image
  (is (instance? BufferedImage (new-image 10 10))))

(deftest test-scale-image
  (let [^BufferedImage bi (new-image 10 10)
        bi (scale-image bi 5 6)]
    (is (instance? BufferedImage bi))
    (is (== 5 (.getWidth bi)))
    (is (== 6 (.getHeight bi)))))

