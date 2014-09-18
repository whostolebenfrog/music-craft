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
           [com.badlogic.gdx.graphics GL20]
           [com.badlogic.gdx.graphics.g3d.utils FirstPersonCameraController]
           [com.badlogic.gdx.math Vector3]))

;; required for the refresh function
(declare music-craft main-screen text-screen)
(def visible-objects (atom 0))
(def degress-per-pixel 0.5)

(defn directional-light
  "Returns a new directional light, doesn't seem to exist in play-clj"
  []
  (doto (DirectionalLight.) (.set 0.8 0.8 0.8 -1 -0.8 -0.2)))

(defn handle-mouse-move
  [screen]
  (let [camera (u/get-obj screen :camera)
        delta-x (* (input! :get-delta-x) degress-per-pixel)
        delta-y (* (input! :get-delta-y) degress-per-pixel)
        vec-y (doto (Vector3.) (.set (.direction camera)) (.crs Vector3/Y))]
    (doto screen
      (perspective! :rotate Vector3/Y delta-x)
      (perspective! :rotate vec-y delta-y)
      (perspective! :update))))

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
    (let [gl2 (graphics! :get-g-l20)]
      (-> gl2 (.glEnable GL20/GL_CULL_FACE))
      (-> gl2 (.glCullFace GL20/GL_BACK)))
    (graphics! :set-continuous-rendering false)
    (input! :set-cursor-catched true)
    (world/blocks))

  :on-mouse-moved
  (fn [screen entities]
    (when (input! :is-cursor-catched)
     (handle-mouse-move screen))
    entities)

  :on-key-down
  (fn [{:keys [key] :as screen} entities]
    (condp = key
      (key-code :w)
      (do
        (perspective! screen :translate (.direction (u/get-obj screen :camera)))
        (perspective! screen :update))

      (key-code :t)
      (input! :set-cursor-catched (not (input! :is-cursor-catched)))

      (key-code :r)
      (on-gl (set-screen! music-craft main-screen text-screen))

      nil)
    entities)

  :on-render
  (fn [screen entities]
    (clear! 0.1 0 0.3 1)
    (let [frus (.frustum (u/get-obj screen :camera))
          visible-entities (filter (fn [{:keys [x y z]}] (.pointInFrustum frus x y z)) entities)]
      (reset! visible-objects (count visible-entities))
      (render! screen visible-entities))
    entities))

(defscreen text-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    (assoc (label "0" (color :black))
           :id :fps
           :x 5))

  :on-render
  (fn [screen entities]
    (->> (for [entity entities]
           (case (:id entity)
             :fps (doto entity (label! :set-text (str (game :fps) " - " @visible-objects)))
             entity))
         (render! screen)))

  :on-resize
  (fn [screen entities]
    (height! screen 300)))

(defgame music-craft
  :on-create
  (fn [this]
    (set-screen! this main-screen text-screen)))
