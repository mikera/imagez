imagez
======

Image processing library for Clojure

Contains various utility functions for handling colours and bitmap images.

[![Build Status](https://travis-ci.org/mikera/imagez.png?branch=develop)](https://travis-ci.org/mikera/imagez)

### Example

```clojure
(use 'mikera.image.core)
(require '[mikera.image.filters :as filt])

;; load an image from a resource file
(def ant (load-image-resource "mikera/image/samples/Ant.png"))

;; show the iamge, after applying an "invert" filter
(show (filter-image ant (filt/invert)))
```

![Inverted ant](http://clojurefun.files.wordpress.com/2013/05/inverted-ant.png)

### Features

Features so far:

- Creating new images
- Scaling / zooming images
- Loading images - from ordinary files, resource files, filesystem paths and streams
- Getting and setting pixels in bulk using primitive arrays
- Filtering images (blur, contrast, brightness etc.)
- Various colour handling functions
- Progressive encoding and controlling the quality of output images.

Imagez is a new library, so the API is being refined. Expect changes / additions on a regular basis. 

### Using Imagez

Simply add the dependency via Clojars: https://clojars.org/net.mikera/imagez

Imagez requires Clojure 1.4 and above.

### More Examples

```clojure
(use 'mikera.image.core)
(use 'mikera.image.colours)

;; create a new image
(def bi (new-image 32 32))

;; gets the pixels of the image, as an int array
(def pixels (get-pixels bi))

;; fill some random pixels with colours
(dotimes [i 1024]
  (aset pixels i (rand-colour)))

;; update the image with the newly changed pixel values
(set-pixels bi pixels)

;; view our new work of art
;; the zoom function will automatically interpolate the pixel values
(show bi :zoom 10.0 :title "Isn't it beautiful?")
```

For more examples including image filtering, see the demo namespace:

 - https://github.com/mikera/imagez/blob/master/src/test/clojure/mikera/image/demo.clj

### License

LGPL version 3.0.
