(ns mikera.image.test-core
  (:use mikera.image.core)
  (:use clojure.test)
  (:import [java.awt.image BufferedImage]))

(deftest test-new-image
  (is (instance? BufferedImage (new-image 10 10))))

