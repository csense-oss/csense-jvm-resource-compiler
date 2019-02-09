package csense.javafx.resource.compiler.properties

import java.io.*
import java.nio.*
import java.nio.channels.*
import java.nio.file.*

//Original copyright
/*
 Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/
// Changes made are visible in GIT, and simply put, optimizations
// There are no warranty as in according with the GPL linked below.


/**
 * see license of this
 * http://openjdk.java.net/legal/gplv2+ce.html
 * To obtain the original code you are to find it though the openJDK in a file called Properties.java
 *
 * Reads java property files but without the old api and not being able to modify them
 * @property innerData HashMap<String, String>
 * @property data Map<String, String>
 * @property formFeed Char
 */
class PropertyReader {

    private val innerData = hashMapOf<String, String>()

    val data: Map<String, String>
        get () = innerData

    val formFeed: Char = '\u000C' //used by printers...

    @Throws(IOException::class)
    private fun load0(lr: LineReader) {
        val convtBuf = CharArray(1024)
        var limit: Int = lr.readLine()
        var keyLen: Int
        var valueStart: Int
        var c: Char
        var hasSep: Boolean
        var precedingBackslash: Boolean

        while (limit >= 0) {
            keyLen = 0
            valueStart = limit
            hasSep = false
            precedingBackslash = false
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen]
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1
                    hasSep = true
                    break
                } else if ((c == ' ' || c == '\t' || c == formFeed) && !precedingBackslash) {
                    valueStart = keyLen + 1
                    break
                }
                precedingBackslash = if (c == '\\') {
                    !precedingBackslash
                } else {
                    false
                }
                keyLen++
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart]
                if (c != ' ' && c != '\t' && c != formFeed) {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true
                    } else {
                        break
                    }
                }
                valueStart++
            }
            val key = if (lr.haveUnicodeInKey) {
                loadConvert(lr.lineBuf, keyLen, convtBuf)
            } else {
                String(lr.lineBuf, 0, keyLen)
            }
            val value = if (lr.haveUnicodeInValue) {
                loadConvert(lr.lineBuf, keyLen, convtBuf)
            } else {
                String(lr.lineBuf, valueStart, limit - valueStart)
            }
//            val key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf)
//            val value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf)
            innerData[key] = value
            limit = lr.readLine()
        }
    }

    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    internal inner class LineReader(private var reader: FileChannel) {

        val inByteBuf = ByteBuffer.allocate(reader.size().toInt())
        var lineBuf = CharArray(1024)
        var inLimit = 0
        var inOff = 0

        var haveUnicodeInKey = false
        var haveUnicodeInValue = false


        @Throws(IOException::class)
        fun readLine(): Int {
            var len = 0
            var c: Char
            haveUnicodeInKey = false
            haveUnicodeInValue = false
            var skipWhiteSpace = true
            var isCommentLine = false
            var isNewLine = true
            var appendedLineBegin = false
            var precedingBackslash = false
            var skipLF = false

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = reader.read(inByteBuf)
                    inOff = 0
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1
                        }
                        if (precedingBackslash) {
                            len--
                        }
                        return len
                    }
                }
                c = (0xff and inByteBuf[inOff++].toInt()).toChar()
                if (skipLF) {
                    skipLF = false
                    if (c == '\n') {
                        continue
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == formFeed) {
                        continue
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue
                    }
                    skipWhiteSpace = false
                    appendedLineBegin = false
                }
                if (isNewLine) {
                    isNewLine = false
                    if (c == '#' || c == '!') {
                        isCommentLine = true
                        continue
                    }
                }

                if (c != '\n' && c != '\r') {
                    if (c == 'u' && precedingBackslash) {
                        haveUnicodeInKey = true
                        haveUnicodeInValue = true
                    }
                    lineBuf[len++] = c
                    if (len == lineBuf.size) {
                        var newLength = lineBuf.size * 2
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE
                        }
                        val buf = CharArray(newLength)
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.size)
                        lineBuf = buf
                    }
                    //flip the preceding backslash flag
                    precedingBackslash = if (c == '\\') {
                        !precedingBackslash
                    } else {
                        false
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false
                        isNewLine = true
                        skipWhiteSpace = true
                        len = 0
                        continue
                    }
                    if (inOff >= inLimit) {
                        inLimit = reader.read(inByteBuf)
                        inOff = 0
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--
                            }
                            return len
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1
                        //skip the leading whitespace characters in following line
                        skipWhiteSpace = true
                        appendedLineBegin = true
                        precedingBackslash = false
                        if (c == '\r') {
                            skipLF = true
                        }
                    } else {
                        return len
                    }
                }
            }
        }
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private fun loadConvert(`in`: CharArray, len: Int, convtBuf: CharArray): String {
        var off = 0
        var convtBuf = convtBuf
        if (convtBuf.size < len) {
            var newLen = len * 2
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE
            }
            convtBuf = CharArray(newLen)
        }
        var aChar: Char
        val out = convtBuf
        var outLen = 0
        val end = off + len

        while (off < end) {
            aChar = `in`[off++]
            if (aChar == '\\') {
                aChar = `in`[off++]
                if (aChar == 'u') {
                    // Read the xxxx
                    var value = 0
                    for (i in 0..3) {
                        aChar = `in`[off++]
                        value = when (aChar) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value shl 4) + aChar.toInt() - '0'.toInt()
                            'a', 'b', 'c', 'd', 'e', 'f' -> (value shl 4) + 10 + aChar.toInt() - 'a'.toInt()
                            'A', 'B', 'C', 'D', 'E', 'F' -> (value shl 4) + 10 + aChar.toInt() - 'A'.toInt()
                            else -> throw IllegalArgumentException(
                                    "Malformed \\uxxxx encoding."
                            )
                        }
                    }
                    out[outLen++] = value.toChar()
                } else {
                    when (aChar) {
                        't' -> aChar = '\t'
                        'r' -> aChar = '\r'
                        'n' -> aChar = '\n'
                        'f' -> aChar = formFeed
                    }
                    out[outLen++] = aChar
                }
            } else {
                out[outLen++] = aChar
            }
        }
        return String(out, 0, outLen)
    }


    companion object {
        fun read(path: Path): PropertyReader = read(path.toFile())

        fun read(file: File): PropertyReader = FileInputStream(file).channel.use { channel: FileChannel ->
            PropertyReader().apply {
                load0(LineReader(channel))
            }
        }
    }
}
