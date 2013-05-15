(ns mikera.image.filters
  (:import [java.awt.image BufferedImage BufferedImageOp]))

;; TODO lot more filters to implement!!
;;
;; see: http://www.jhlabs.com/ip/filters/index.html

(defn apply-mask
  "Creates an apply-mask filter"
  (^BufferedImageOp [^BufferedImage mask ^BufferedImage destination]
    (com.jhlabs.image.ApplyMaskFilter. mask destination)))

(defn blur
  "Creates a simple blur filter (3x3 pixel) blur"
  (^BufferedImageOp []
    (com.jhlabs.image.BlurFilter.)))

(defn box-blur
  "Creates a box-blur filter "
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

(defn invert
   "Creates a colour inversion filter"
   (^BufferedImageOp []
     (com.jhlabs.image.InvertFilter.)))

(defn emboss
   "Creates a colour inversion filter"
   (^BufferedImageOp []
     (com.jhlabs.image.EmbossFilter.)))

(defn halftone
   "Creates a halftone filter"
   (^BufferedImageOp []
     (com.jhlabs.image.HalftoneFilter.)))

(defn noise
   "Creates a noise filter"
   (^BufferedImageOp []
     (com.jhlabs.image.NoiseFilter.)))