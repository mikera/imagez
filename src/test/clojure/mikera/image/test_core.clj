(ns mikera.image.test-core
  (:use mikera.image.core)
  (:use clojure.test)
  (:require [mikera.image.colours :refer [long-colour]]
            [clojure.java.io :refer [as-file resource input-stream]])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO
           javax.imageio.ImageWriter
           javax.imageio.ImageWriteParam))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(deftest test-new-image
  (is (instance? BufferedImage (new-image 10 10)))
  (is (instance? BufferedImage (new-image 10 10 false)))
  (is (= BufferedImage/TYPE_INT_ARGB (.getType (new-image 10 10))))
  (is (= BufferedImage/TYPE_INT_RGB (.getType (new-image 10 10 false)))))

(deftest test-scale-image
  (let [^BufferedImage bi (new-image 10 10)
        bi (scale-image bi 5 6)]
    (is (instance? BufferedImage bi))
    (is (== 5 (width bi)))
    (is (== 6 (height bi)))))

(deftest test-rotate-image
  (let [bi (new-image 10 5)
        r90 (rotate bi 90)
        r180 (rotate bi 180)
        r270 (rotate bi 270)]
    (is (instance? BufferedImage r90))
    (is (instance? BufferedImage r180))
    (is (instance? BufferedImage r270))
    (is (== 5  (.getWidth r90)))
    (is (== 10 (.getHeight r90)))
    (is (== 10 (.getWidth r180)))
    (is (== 5  (.getHeight r180)))
    (is (== 5  (.getWidth r270)))
    (is (== 10 (.getHeight r270)))))

(deftest test-scale
  (let [^BufferedImage bi (new-image 10 10)
        bi (scale bi 2.0 3.0)]
    (is (instance? BufferedImage bi))
    (is (== 20 (.getWidth bi)))
    (is (== 30 (.getHeight bi)))))

(deftest test-zoom-image
  (let [^BufferedImage bi (new-image 10 10)
        bi (zoom bi 2.0)]
    (is (instance? BufferedImage bi))
    (is (== 20 (.getWidth bi)))
    (is (== 20 (.getHeight bi)))
    (.setRGB bi 0 0 (unchecked-int 0xFFFFFFFF))
    (let [si (sub-image bi 0 0 3 4)]
      (is (== 3 (.getWidth si)))
      (is (== 4 (.getHeight si)))
      (is (== 0xFFFFFFFF (long-colour (.getRGB si 0 0)))))))

(deftest test-get-pixels
  (let [bi (new-image 1 1)
        pxs (get-pixels bi)]
    (is (instance? (Class/forName "[I") pxs))
    (is (= [0] (seq pxs)))
    (aset pxs 0 0xFFFFFFFF)
    (set-pixels bi pxs)
    (is (== 0xFFFFFFFF (long-colour (.getRGB bi 0 0))))))

(deftest test-ensure-default-image-type
  (let [i (ImageIO/read (as-file "src/test/resources/mikera/image/samples/Clojure_300x300.png"))]
    (is (not= BufferedImage/TYPE_INT_ARGB (.getType i)))
    (is (= BufferedImage/TYPE_INT_ARGB (.getType (ensure-default-image-type i))))))

(deftest test-load-image
  (testing "image loading"
    (are [r] (instance? BufferedImage (load-image r))
         "src/test/resources/mikera/image/samples/Clojure_300x300.png"
         (as-file "src/test/resources/mikera/image/samples/Clojure_300x300.png")
         (ImageIO/read (as-file "src/test/resources/mikera/image/samples/Clojure_300x300.png"))
         (resource "mikera/image/samples/Clojure_300x300.png")
         (input-stream "src/test/resources/mikera/image/samples/Clojure_300x300.png")))
  (testing "load image type conversion"
    (is (= BufferedImage/TYPE_INT_ARGB (.getType (load-image "src/test/resources/mikera/image/samples/Clojure_300x300.png"))))))

(deftest test-compression
  (testing "png"
    (let [^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "png"))
          param (.getDefaultWriteParam writer)]
      ;; setting compression quality is not actually supported for PNG, this test
      ;; is just validates that it doesn't crash
      (is (= param (#'mikera.image.core/apply-compression param 1.0 "png")))))

  (testing "jpeg"
    (let [^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "jpeg"))
          param (.getDefaultWriteParam writer)
          compression-fn #'mikera.image.core/apply-compression]
      (is (= param (#'mikera.image.core/apply-compression param 1.0 "jpeg")))
      (is (= (.getCompressionQuality ^ImageWriteParam (compression-fn param 1.0 "jpeg")) 1.0))
      (is (= (.getCompressionQuality ^ImageWriteParam (compression-fn param 0.5 "jpeg")) 0.5))))

  (testing "gif"
    (let [^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "gif"))
          param (.getDefaultWriteParam writer)
          compression-fn #'mikera.image.core/apply-compression]
      (is (= param (#'mikera.image.core/apply-compression param 1.0 "gif")))
      (is (= (.getCompressionType ^ImageWriteParam (compression-fn param 1.0 "gif")) "LZW")))))

(deftest test-progressive
  (let [^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "jpeg"))
        param (.getDefaultWriteParam writer)
        progressive-fn #'mikera.image.core/apply-progressive]
    (testing "returns the param"
      (is (= param (progressive-fn param true))))

    (testing "progressive flag values"
      (is (= (.getProgressiveMode ^ImageWriteParam (progressive-fn param true)) ImageWriteParam/MODE_DEFAULT))
      (is (= (.getProgressiveMode ^ImageWriteParam (progressive-fn param false)) ImageWriteParam/MODE_DISABLED))
      (is (= (.getProgressiveMode ^ImageWriteParam (progressive-fn param nil)) ImageWriteParam/MODE_COPY_FROM_METADATA)))))
