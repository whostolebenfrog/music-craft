(ns music-craft.core
  (:require [clojure.math.combinatorics :as combo]
            [clojure.java.io :as io]
            [music-craft.world :as world]
            [pjson.core :as json]
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

;; required for the refresh functions
(declare music-craft main-screen text-screen menu-screen)
(def visible-objects (atom 0))
(def degress-per-pixel 0.2)
(def menu-selected (atom 1))
(def current-track (atom nil))
(def playing-track (atom nil))

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

(defn stop-track
  "stop playing the current track"
  []
  (when @playing-track
    (sound! @playing-track :stop)
    (reset! playing-track nil)))

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
    (when @current-track
      (stop-track)
      (reset! playing-track (sound (:file @current-track) :play)))
    (graphics! :set-continuous-rendering false)
    (input! :set-cursor-catched true)
    (world/blocks (when @current-track (-> (:info @current-track) io/resource slurp json/parse-string))))

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

      (key-code :u)
      (do
        (stop-track)
        (on-gl (set-screen! music-craft menu-screen)))

      nil)
    entities)

  :on-render
  (fn [screen entities]
    (clear! 0.1 0 0.3 1)
    (let [frus (.frustum (u/get-obj screen :camera))
          visible-entities (filter (fn [{:keys [x y z]}] (.sphereInFrustum frus x y z 1)) entities)]
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

(defn rotate-menu
  [entities direction]
  (let [menu-items (count (filter :menu? entities))
        old-active @menu-selected
        new-active (reset! menu-selected (mod (direction @menu-selected) menu-items))]
    (for [entity entities]
      (if (:menu? entity)
        (condp = (:pos entity)
          old-active (-> (assoc-in entity [:active?] false)
                         (doto (label! :set-color (color :white))))
          new-active (-> (assoc-in entity [:active?] true)
                         (doto (label! :set-color (color :green))))
          entity)
        entity))))

(defn menu-option
  [pos text & [{:keys [menu-color menu? active? file info]
                :or {menu-color (color :white)
                     menu? true
                     active? false}}]]
  (assoc (label "0" menu-color :set-text text)
    :menu? menu? :pos pos :x 5 :y (* pos 20) :active? active? :file file :info info))

(defscreen menu-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    (input! :set-cursor-catched true)
    [(menu-option 0 "ambient.mp3" {:file "ambient1.mp3" :info "ambient1.info"})
     (menu-option 1 "metal.mp3"   {:file "metal1.mp3"   :info "metal1.info"})
     (menu-option 2 "blues.mp3"   {:file "blues1.mp3"   :info "blues1.info" :active? true })
     (menu-option 3 "-----------" {:menu? false})
     (menu-option 4 "music-craft" {:menu-color (color :red)
                                   :menu? false})])

  :on-key-down
  (fn [{:keys [key] :as screen} entities]
    (condp = key
      (key-code :t)
      (do
        (input! :set-cursor-catched (not (input! :is-cursor-catched)))
        entities)

      (key-code :r)
      (do
        (on-gl (set-screen! music-craft menu-screen))
        entities)

      (key-code :enter)
      (do
        (reset! current-track (find-first :active? entities))
        (on-gl (set-screen! music-craft main-screen text-screen)))

      (key-code :dpad-up)
      (rotate-menu entities inc)

      (key-code :dpad-down)
      (rotate-menu entities dec)

      entities))

  :on-render
  (fn [screen entities]
    (clear! 0 0 0 1)
    (render! screen entities))

  :on-resize
  (fn [screen entities]
    (height! screen 300)))

(defgame music-craft
  :on-create
  (fn [this]
    (set-screen! this menu-screen)))
