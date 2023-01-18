package com.vaani.models

import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

data class PlayBack(var mediaPlayer: MediaPlayer?, var file: File?, var vlc: LibVLC?)
