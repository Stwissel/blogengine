# BlogEngine

## Overview
is a static Blog renderer. It uses *.blog files and *.mustache templates to render html output.
A blog file starts with a YAML front-matter describing a blog entry and eventually its old URL,
followed by content in either HTML or MARKDOWN. 

## Comments
It reads related comments and merges them into the Blog article, so the static page can be completely spidered by a search engine.
Blog comments are captured in JSON format like the one produced by [the git based comment module](https://github.com/Stwissel/blog-comments-public)

## Build script
It is designed to run as the build process, triggered by a GIT commit
