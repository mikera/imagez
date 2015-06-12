(ns mikera.image.test-io
  (:use mikera.image.core)
  (:use clojure.test)
  (:require [clojure.java.io :refer [as-file resource input-stream]])
  (:import javax.imageio.ImageIO
           javax.imageio.ImageWriter
           javax.imageio.ImageWriteParam))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(deftest test-load-image
  (testing "image loading"
    (are [r] (instance? java.awt.image.BufferedImage (load-image r))
         "src/test/resources/mikera/image/samples/Clojure_300x300.png"
         (as-file "src/test/resources/mikera/image/samples/Clojure_300x300.png")
         (ImageIO/read (as-file "src/test/resources/mikera/image/samples/Clojure_300x300.png"))
         (resource "mikera/image/samples/Clojure_300x300.png")
         (input-stream "src/test/resources/mikera/image/samples/Clojure_300x300.png")))
  (testing "load image type conversion"
    (is (= java.awt.image.BufferedImage/TYPE_INT_ARGB (.getType (load-image "src/test/resources/mikera/image/samples/Clojure_300x300.png"))))))

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
      (is (= (.getCompressionQuality ^ImageWriteParam (compression-fn param 0.5 "jpeg")) 0.5))

  (testing "gif"
    (let [^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "gif"))
          param (.getDefaultWriteParam writer)
          compression-fn #'mikera.image.core/apply-compression]
      (is (= param (#'mikera.image.core/apply-compression param 1.0 "gif")))
      (is (= (.getCompressionType ^ImageWriteParam (compression-fn param 1.0 "gif")) "LZW")))))))
