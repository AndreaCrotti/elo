(ns byf.pages.utils
  (:import (java.util UUID)))

(defn cache-buster
  [path]
  ;; fallback to a random git sha when nothing is found
  (format "%s?git_sha=%s"
          path
          ;; TODO: can we set the git commit again if not on Heroku?
          (str (UUID/randomUUID))))
