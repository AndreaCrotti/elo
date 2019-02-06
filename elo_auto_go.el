(require 'projectile)
(require 'cider)

(defun fa-clojure-repl? ()
  (not (null (member cider-repl-type '(clj "clj")))))

(defun fa-start-finops-admin ()
  (interactive)
  (cider-switch-to-repl-buffer)
  (when
      (and
       (fa-clojure-repl?)
       (equal (projectile-project-name) "elo"))

    (insert "(ig/go)")
    (cider-repl-return)))

(add-hook 'cider-connected-hook 'fa-start-finops-admin)

(provide 'elo-auto-go)
