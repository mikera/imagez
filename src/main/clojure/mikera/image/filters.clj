(ns mikera.image.filters
  (:import [java.awt.image BufferedImage BufferedImageOp]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;; TODO lot more filters to implement!!
;;
;; see: http://www.jhlabs.com/ip/filters/index.html

(deftype Filter [^BufferedImageOp image-op]
  clojure.lang.IFn
    (invoke [this image]
      (let [^BufferedImage image image 
            dest-img (.createCompatibleDestImage image-op image (.getColorModel image))]
        (.filter image-op image dest-img)
        dest-img))
    (applyTo [this args] 
      (clojure.lang.AFn/applyToHelper this args)))

(defn to-image-op 
  "Converts an Imagez filer to a java.awt.image.BufferedImageOp"
  (^BufferedImageOp [filter]
    (if (instance? BufferedImageOp filter)
      filter
      (.image-op ^Filter filter))))

(defn apply-mask
  "Creates an apply-mask filter"
  (^Filter [^BufferedImage mask ^BufferedImage destination]
    (Filter. (com.jhlabs.image.ApplyMaskFilter. mask destination))))

(defn blur
  "Creates a simple blur filter (3x3 pixel) blur"
  (^Filter []
    (Filter. (com.jhlabs.image.BlurFilter.))))

(defn contrast
  "Creates a contrast filter"
  (^Filter [contrast]
    (let [f (com.jhlabs.image.ContrastFilter.)]
      (.setContrast f (float contrast))
      (Filter. f))))

(defn brightness
  "Creates a brightness filter"
  (^Filter [brightness]
    (let [f (com.jhlabs.image.ContrastFilter.)]
      (.setBrightness f (float brightness))
      (Filter. f))))

(defn box-blur
  "Creates a box-blur filter "
  (^Filter []
    (Filter. (com.jhlabs.image.BoxBlurFilter.)))
  (^Filter [hRadius vRadius & {:keys [iterations]
                               :or {iterations 3}}]
    (Filter. (com.jhlabs.image.BoxBlurFilter. (float hRadius) (float vRadius) iterations))))

(defn quantize
  "Creates a quantization filter (reduces to a given number of levels)"
  (^Filter []
    (Filter. (com.jhlabs.image.QuantizeFilter.)))
  (^Filter [num-levels & {:keys [dither serpentine]
                                   :or {dither false
                                        serpentine false}}]
    (let [f (com.jhlabs.image.QuantizeFilter.)]
      (.setNumColors f (int num-levels))
      (.setSerpentine f (boolean serpentine))
      (.setDither f (boolean serpentine))
      (Filter. f))))

(defn grayscale
   "Creates a grayscale filter"
   (^Filter []
     (Filter. (com.jhlabs.image.GrayscaleFilter.))))

(defn invert
   "Creates a colour inversion filter"
   (^Filter []
     (Filter. (com.jhlabs.image.InvertFilter.))))

(defn emboss
   "Creates an emboss filter"
   (^Filter []
     (Filter. (com.jhlabs.image.EmbossFilter.))))

(defn halftone
   "Creates a halftone filter"
   (^Filter []
     (Filter. (com.jhlabs.image.HalftoneFilter.))))

(defn noise
   "Creates a noise filter"
   (^BufferedImageOp []
     (Filter. (com.jhlabs.image.NoiseFilter.))))