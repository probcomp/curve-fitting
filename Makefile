.PHONY: dev
dev:
	clj -Adev

nrepl:
	clj -Adev -Sdeps '{:deps {nrepl {:mvn/version "0.4.5"} cider/cider-nrepl {:mvn/version "0.18.0"}}}' -m nrepl.cmdline --middleware "[cider.nrepl/cider-middleware]"
