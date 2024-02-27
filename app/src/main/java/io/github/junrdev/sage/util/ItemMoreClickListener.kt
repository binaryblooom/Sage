package io.github.junrdev.sage.util

interface ItemMoreClickListener<T> {
    fun onClickMain(t : T)
    fun onClickMore(t : T)
}