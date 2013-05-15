(ns mikera.image.filters
  (:import [java.awt.image BufferedImage BufferedImageOp]))

(defn box-blur
  "Returns a box-blur filter"
  (^BufferedImageOp []
    (com.jhlabs.image.BoxBlurFilter.)))