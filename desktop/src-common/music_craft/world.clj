(ns music-craft.world
  (:require [clojure.java.io :as io]
            [play-clj
             [core :refer :all]
             [ui :refer :all]
             [g2d :as g2d]
             [g3d :refer :all]
             [math :refer :all]
             [repl :refer :all]
             [utils :as u]])
  (:import [clisk.noise Simplex]
           [com.badlogic.gdx.files FileHandle]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g3d.attributes TextureAttribute]))

(defn noise-for-grid
  "Returns a grid of noise values between min and max for grid x-size and y-size"
  [x-size y-size min max seed]
  (Simplex/seed seed)
  (let [output (make-array Integer/TYPE x-size y-size)]
    (doseq [x (range 0 x-size)
            y (range 0 y-size)]
      (let [nor-x (/ x x-size)
            nor-y (/ y y-size)
            noise (Simplex/noise nor-x nor-y)
            r-height (Math/round (* noise (- max min)))
            min-height (+ min r-height)]
        (aset output x y (Integer. min-height))))
    output))

;; constants
(def b-size "block size" 1)
(def grid-size 40)

;; textures
(defn raw-texture
  "Creates a libgdx texture from the supplied resourece path"
  [path]
  (-> path io/resource .toURI java.io.File. FileHandle. Texture.))

(defn load-material
  "Creates a new material from a texture"
  [path]
  (material :set (TextureAttribute. TextureAttribute/Diffuse (raw-texture path))))

(def grass-material  (delay (load-material "grass2.jpg")))
(def stone-material  (delay (load-material "stone.jpg")))
(def stone2-material (delay (load-material "stone2.jpg")))
(def sand-material   (delay (load-material "sand.jpg")))
(def water-material  (delay (load-material "water.jpg")))
(def fire-material   (delay (load-material "fire.jpg")))

(def random
  "Our random number generator"
  (java.util.Random.))

(defn weighted-gaussian-random
  "Returns a weighted normally distributed random number between 0 and 1"
  [weight]
  (min (* weight (Math/abs (.nextGaussian random))) 1))

(defn nearest-material
  "Find the nearest material to the supplied number"
  [n mats]
  (apply min-key (fn [{:keys [energy]}] (Math/abs (- n energy))) mats))


(defn random-material
  "Returns a random texture"
  [energy]
  (let [energy-mats [{:mat grass-material :energy 0.2}
                     {:mat stone-material :energy 0.6}
                     {:mat sand-material  :energy 0}
                     {:mat water-material :energy 0.3}
                     {:mat stone2-material :energy 0.8}
                     {:mat fire-material :energy 1}]]

    (:mat (nearest-material (weighted-gaussian-random energy) energy-mats))))

(def builder (model-builder))

(defn block
  "Creates a block at pos x, y, z with a random texture"
  [x y z energy]
  (let [model-attrs (bit-or (usage :position) (usage :normal) (usage :texture-coordinates))]
    (-> (model-builder! builder :create-box b-size b-size b-size @(random-material energy) model-attrs)
        model
        (assoc :x x :y y :z z))))

(defn blocks
  "Create the block entities"
  [info]
  (let [energy (info "energy")
        seed (info "danceability")
        min 1
        max (Math/round (* energy 15))
        noise (noise-for-grid grid-size grid-size min max seed)]
    (vec (for [x (range grid-size)
               z (range grid-size)
               y (range 0 (aget noise x z))]
           (block x y z energy)))))
