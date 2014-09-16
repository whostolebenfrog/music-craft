(ns music-craft.core
  (:require [clojure.math.combinatorics :as combo]
            [music-craft.world :as world]
            [play-clj
             [core :refer :all]
             [ui :refer :all]
             [g2d :as g2d]
             [g3d :refer :all]
             [math :refer :all]
             [repl :refer :all]
             [utils :as u]])
  (:import [com.badlogic.gdx.graphics.g3d.environment DirectionalLight]
           [com.badlogic.gdx.graphics.g3d.utils FirstPersonCameraController]))

;; required for the refresh function
(declare music-craft main-screen)

(defn directional-light
  "Returns a new directional light, doesn't seem to exist in play-clj"
  []
  (doto (DirectionalLight.) (.set 0.8 0.8 0.8 -1 -0.8 -0.2)))

(defn translate-camera
  "Translate the camera on the x, y and z axis"
  [screen x y z]
  (doto screen
    (perspective! :translate x y z)
    (perspective! :update)))

(def last-mouse-pos (atom [nil nil]))

(defn rotate-camera
  "Rotate the camera around the various axis"
  [x y screen]
  (doto screen
    (perspective! :rotate x 0 1 0)
    (perspective! :rotate y -1 0 0)
    (perspective! :update)))

(defn handle-mouse-move
  ""
  [screen]
  (let [[last-x last-y] @last-mouse-pos
        x (game :point-x)
        y (game :point-y)]
    (when-not (nil? last-x)
      (rotate-camera (- last-x x) (- last-y y) screen))
    (reset! last-mouse-pos [x y])))

(def fp-camera-controller (atom nil))

;; http://stackoverflow.com/questions/21825959/libgdx-first-person-camera-controll
;; https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/FirstPersonCameraController.java
(defn update-camera
  [screen]
  (when-not @fp-camera-controller
    (reset! fp-camera-controller (FirstPersonCameraController. (u/get-obj screen :camera))))
  (.update @fp-camera-controller))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen
             :renderer (model-batch)
             :attributes (let [ambient-attr-type (attribute-type :color :ambient-light)
                               ambient-attr (attribute :color ambient-attr-type 0.2 0.2 0.2 1)]
                           (environment :set ambient-attr :add (directional-light)))
             :camera (doto (perspective 67 (game :width) (game :height))
                       (position! 40 15 40)
                       (direction! 0 0 0)
                       (near! 0.1)
                       (far! 100)))
    (world/blocks))

  :on-render
  (fn [screen entities]
    (clear! 0.1 0 0.3 1)
    (update-camera screen)
    (render! screen entities)))

(comment  :on-mouse-moved
          (fn [screen entities]
            (handle-mouse-move screen)
            entities))
  (comment
   ;; Here we are handling some basic movement commands, ideally we would be using the mouse
   ;; here but currently I'm struggling to see how to add mouse cursor locking into
   ;; play-clj. I'll get back to this.
    :on-key-down
    (fn [{:keys [key] :as screen} entities]
      (condp = key
        (key-code :w)
        (translate-camera screen 0 0 -1)

        (key-code :s)
        (translate-camera screen 0 0 1)

        (key-code :a)
        (translate-camera screen -1 0 0)

        (key-code :d)
        (translate-camera screen 1 0 0)

        (key-code :j)
        (translate-camera screen 0 -1 0)

        (key-code :k)
        (translate-camera screen 0 1 0)

        (key-code :r)
        (on-gl (set-screen! music-craft main-screen))

        nil)
      entities))

(defgame music-craft
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
