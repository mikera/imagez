(ns mikera.image.demo
  (:use mikera.image.colours)
  (:use mikera.image.core)
  (:use mikera.image.filters)
  (:use mikera.image.spectrum)
  (:use mikera.image.dither)
  (:use mikera.image.gui)
  (:require [clojure.java.io :refer [resource]]))

;; load an image from a packaged resouce on the classpath
(def ant (-> "mikera/image/samples/Ant.png" resource load-image))

(defn demo []

	 ;; show a basic image
	(show ant)

	;; demo of various filters used in functional style
	(show ((grayscale) ant))
	(show ((box-blur 2 2) ant))
	(show ((contrast 0.5) ant))
	(show ((brightness 2.0) ant))
	(show ((invert) ant))
 
  ;; dithering
  (show (dither ant (colour-palette-function 4)))
  (show (dither ant (mono-palette-function)))

	;; demo of visualising a colour gradient
	(show (gradient-image wheel))

        (save ant "/path/to/new-ant.png" :quality 0.9 :progressive true)
    
  ;; change pixels on an image, using colour components     
  (let [temp (copy ant)]
    (dotimes [x 256]
      (with-components [[r g b] (get-pixel ant x 20)]
        (set-pixel temp x 20 (rgb-from-components b x r))))
    (show temp))     
)
