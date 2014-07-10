(ns mikera.image.colours
  (:use [mikera.cljutils error])
  (:import [mikera.image Colours])
  (:import [mikera.util Rand])
  (:import [java.awt Color]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defmacro long-colour
  "Macro to convert an integer ARGB value to a long colour value."
  ([x]
  `(bit-and 0xFFFFFFFF ~x)))

(defn rgb
  "Get the integer ARGB colour value specified by the RGB colour components.

   Unless specified the Alpha value of the resulting colour will be 1.0 (fully opaque)"
  (^long [^Color colour]
    (bit-or 0xFF000000 (long-colour (.getRGB colour))))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getARGBClamped (double a) (double r) (double g) (double b)))))

(defn argb
  "Get the integer ARGB colour value specified by the ARGB colour components."
  (^long [^Color colour]
    (long-colour (.getRGB colour)))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getARGBClamped (double a)  (double r) (double g) (double b)))))

(defn components-argb
  "Return the ARGB components of a colour value, in a 4-element vector of long values"
  ([^long argb]
   [(bit-shift-right (bit-and argb 0xFF000000) 24)
    (bit-shift-right (bit-and argb 0x00FF0000) 16)
    (bit-shift-right (bit-and argb 0x0000FF00) 8)
    (bit-and argb 0x000000FF)]))

(defn components-rgb
  "Return the RGB components of a colour value, in a 3-element vector of long values"
  ([^long argb]
   [(bit-shift-right (bit-and argb 0x00FF0000) 16)
    (bit-shift-right (bit-and argb 0x0000FF00) 8)
    (bit-and argb 0x000000FF)]))

(defn rand-colour
  "Returns a random RGB colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (Rand/r 0x1000000))))

(defn rand-grayscale
  "Returns a random grayscale colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (* 0x10101 (Rand/r 0x100)))))

(defn color
  (^Color [rgba]
    (let [rgba (int rgba)]
      (Color. rgba true))))

(def JAVA-COLOURS
  '(black blue cyan darkGray gray green lightGray magenta
    orange pink red white yellow))

(doseq [colour JAVA-COLOURS]
  (eval `(def ~(vary-meta colour assoc :const true) (long-colour (.getRGB (. Color ~colour))))))
