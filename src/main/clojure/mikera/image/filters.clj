(ns mikera.image.filters
  (:import [java.awt.image BufferedImage BufferedImageOp]))

(defn apply-mask
  "Creates an apply-mask filter"
  (^BufferedImageOp [^BufferedImage mask ^BufferedImage destination]
    (com.jhlabs.image.ApplyMaskFilter. mask destination)))

(defn box-blur
  "Creates a box-blur filter"
  (^BufferedImageOp []
    (com.jhlabs.image.BoxBlurFilter.))
  (^BufferedImageOp [hRadius vRadius & {:keys [iterations]
                                        :or {iterations 3}}]
    (com.jhlabs.image.BoxBlurFilter. (float hRadius) (float vRadius) iterations)))

(defn quantize
  "Creates a quantization filter (reduces to a given number of levels)"
  (^BufferedImageOp []
    (com.jhlabs.image.QuantizeFilter.))
  (^BufferedImageOp [num-levels & {:keys [dither serpentine]
                                   :or {dither false
                                        serpentine false}}]
    (let [f (com.jhlabs.image.QuantizeFilter.)]
      (.setNumColors f (int num-levels))
      (.setSerpentine f (boolean serpentine))
      (.setDither f (boolean serpentine))
      f)))

(defn grayscale
   "Creates a grayscale filter"
   (^BufferedImageOp []
     (com.jhlabs.image.GrayscaleFilter.)))