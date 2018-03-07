(ns mikera.image.colours
  "Namespace for colour handling and conversion functions.

   Note there are 4 different ways to specify colours:
   1. long colour values (entire colour expressed as a single long) - e.g. 0xFF00FF00
   2. long component values (individual alpha, red, green, blue) - e.g. [255 234 0 101]
   3. double colour values (individual alpha, red, green, blue) - e.g. [0.5 0.5 0.5]
   4. Java Color instances - e.g. java.awt.Color/BLACK

   Care should be taken to use the appropriate function. Typically:
   1. Offers the best performance
   2. Useful for manipulating 1.
   3. Useful for high precision computations, or colours outside normal ranges
   4. Useful for Java interop"
  (:use [mikera.cljutils error])
  (:import [mikera.image Colours])
  (:import [mikera.util Rand])
  (:import [java.awt Color]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defmacro long-colour
  "Convenience macro to cast an integer ARGB value to a long colour value."
  ([x]
  `(bit-and 0xFFFFFFFF ~x)))

;; A cache of boxed Double values
;; This is needed to avoid new allocations of boxed Doubles when converting long component values
(def ^{:static true
       :tag "[Ljava.lang.Double;"} 
     DOUBLE-CACHE 
  (into-array Double (map (fn [x] (/ x 255.0)) (range 256))))

;; A cache of boxed Long values
;; This is needed to avoid new allocations of boxed longs 
(def ^{:static true
       :tag "[Ljava.lang.Long;"} 
     LONG-CACHE 
  (into-array Long (map (fn [x] (long x)) (range 256))))


(defmacro boxed-double-value
  "Convenient macro to convert a long component value to a boxed java.lang.Double. Uses a cache of Double values."
  ([x]
    `(aget DOUBLE-CACHE (long ~x))))

(defmacro boxed-long-value
  "Convenient macro to convert a long component value to a boxed java.lang.Long. Uses a cache of Long values."
  ([x]
    `(aget LONG-CACHE (long ~x))))

(defn rgb
  "Get the long ARGB colour value specified by the RGB colour values (expresed in range 0.0-1.0).

   The Alpha value of the resulting colour will be 1.0 (fully opaque)"
  (^long [^Color colour]
    (bit-or 0xFF000000 (long-colour (.getRGB colour))))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b)))))

(defn argb
  "Get the long ARGB colour value specified by the ARGB colour values (expresed in range 0.0-1.0).

   If not specified specified the Alpha value of the resulting colour will be 1.0 (fully opaque)"
  (^long [^Color colour]
    (long-colour (.getRGB colour)))
  (^long [r g b]
    (long-colour (Colours/getRGBClamped (double r) (double g) (double b))))
  (^long [r g b a]
    (long-colour (Colours/getARGBClamped (double a)  (double r) (double g) (double b)))))

(defmacro rgb-from-components
  "Gets the long colour value from combining red, green and blue long component values.
   The Alpha value of the resulting colour will be fixed at 255 (fully opaque)
   This is implemented as a macro for performance reasons."
  ([r g b]
    `(-> 0xFF000000
       (bit-or (bit-shift-left (bit-and (long ~r) 0xFF) 16))
       (bit-or (bit-shift-left (bit-and (long ~g) 0xFF) 8))  
       (bit-or (bit-and (long ~b) 0xFF))))
  ([r g b a]
    `(-> 0xFF000000
       (bit-or (bit-shift-left (bit-and (long ~r) 0xFF) 16))
       (bit-or (bit-shift-left (bit-and (long ~g) 0xFF) 8))  
       (bit-or (bit-and (long ~b) 0xFF)))))

(defmacro argb-from-components
  "Gets the long colour value from combining red, green and blue long component values.
   If not specified specified the Alpha value of the resulting colour will be 255 (fully opaque)
   This is implemented as a macro for performance reasons."
  ([r g b]
    `(-> 0xFF000000
       (bit-or (bit-shift-left (bit-and (long ~r) 0xFF) 16))
       (bit-or (bit-shift-left (bit-and (long ~g) 0xFF) 8))  
       (bit-or (bit-and (long ~b) 0xFF))))
  ([r g b a]
    `(-> (bit-shift-left (bit-and (long ~a) 0xFF) 24)
       (bit-or (bit-shift-left (bit-and (long ~r) 0xFF) 16))
       (bit-or (bit-shift-left (bit-and (long ~g) 0xFF) 8))  
       (bit-or (bit-and (long ~b) 0xFF)))))

(defmacro extract-alpha
  "Extracts the long alpha component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0xFF000000) 24)))

(defmacro extract-red
  "Extracts the long red component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0x00FF0000) 16)))

(defmacro extract-green
  "Extracts the long green component (range 0-255) from a long colour"
  ([argb]
    `(bit-shift-right (bit-and (long ~argb) 0x0000FF00) 8)))

(defmacro extract-blue
  "Extracts the long blue component (range 0-255) from a long colour"
  ([argb]
    `(bit-and (long ~argb) 0x000000FF)))

(defmacro with-components
  "Macro which extracts the ARGB colour components from a long colour value and binds them to the specified symbols.
  This is implemented as a macro for performance reasons.

  Intended usage: (with-components [[r g b] some-argb-value] .....)"
  ([[syms argb] & body]
    (when-not (vector? syms) (error "with-components expects a vector of symbols to bind"))
    (let [argbsym (gensym)]
      `(let [~argbsym (long ~argb)
             ~@(mapcat (fn [sym mf] [sym (list mf argbsym)]) 
                       syms 
                       [`extract-red `extract-green `extract-blue `extract-alpha])]
         ~@body))))

(defn components-argb
  "Gets the red, green, blue and alpha components of a long colour value. 
   Returns a 4-element vector of long component values (range 0-255)"
  ([^long argb]
   [(extract-red argb)
    (extract-green argb)
    (extract-blue argb)
    (extract-alpha argb)]))

(defn components-rgb
  "Return the RGB components of a long colour value, in a 3-element vector of long component values (range 0-255)"
  ([^long rgb]
   [(extract-red rgb)
    (extract-green rgb)
    (extract-blue rgb)]))

(defn values-rgb
  "Return the RGB components of a long colour value, in a 3-element vector of double values (range 0.0-1.0)"
  ([^long rgb]
   [(boxed-double-value (extract-red rgb))
    (boxed-double-value (extract-green rgb))
    (boxed-double-value (extract-blue rgb))]))

(defn values-argb
  "Gets the red, green, blue and alpha components of a long colour value. 
   Returns a 4-element vector of double values (range 0.0-1.0)"
  ([^long argb]
   [(boxed-double-value (extract-red argb))
    (boxed-double-value (extract-green argb))
    (boxed-double-value (extract-blue argb))
    (boxed-double-value (extract-alpha argb))]))

(defn rand-colour
  "Returns a random RGB colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (Rand/r 0x1000000))))

(defn rand-grayscale
  "Returns a random grayscale colour value with 100% alpha"
  (^long []
    (bit-or 0xFF000000 (* 0x10101 (Rand/r 0x100)))))

(defn color
  "Creates a java.awt.Color instance representing the given ARGB long colour values"
  (^Color [^long argb]
    (Color. (unchecked-int argb) true)))

(defn to-java-color
  "Coerces a colour value to a Java Color instance"
  (^Color [colour]
    (cond
      (instance? Color colour) colour
      (integer? colour) (Color. (unchecked-int colour) true)
      ;; TODO: vactors of colour values / components
      :else (error "Don't know how to convert to Java colour: " (class colour)))))

(def JAVA-COLOURS
  '(black blue cyan darkGray gray green lightGray magenta
    orange pink red white yellow))

(doseq [colour JAVA-COLOURS]
  (eval `(def ~(vary-meta colour assoc :const true) (long-colour (.getRGB (. Color ~colour))))))

(def clear (long-colour 0))
