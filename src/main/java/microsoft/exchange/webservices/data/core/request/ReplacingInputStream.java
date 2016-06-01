package microsoft.exchange.webservices.data.core.request;

/**
 * Created by MrGuy on 1/17/16.
 */

import java.io.*;
import java.util.*;

class ReplacingInputStream extends FilterInputStream {

    // Contains bytes that was processed already
    LinkedList<Integer> outQueue = new LinkedList<Integer>();

    protected ReplacingInputStream(InputStream in) {
        super(in);
    }

    private boolean removeIllegalBytesIfNeeded(StringBuffer stringBuffer, boolean isHex) throws IOException {
        int numberStartPos = isHex ? 3 : 2;
        int numberBase = isHex ? 16 : 10;

        // Cut the number as a string
        String numStr = stringBuffer.substring(numberStartPos, stringBuffer.length() - 1);

        // Convert the string number to a decimal integer
        int numInt = Integer.parseInt(numStr, numberBase);

        if (isLegalXML10Char(numInt)) {
            return false;
        } else {
            // Remove whole character reference bytes
            removeIllegalBytes(stringBuffer.length());

            return true;
        }
    }

    private void removeIllegalBytes(int count) {
        for (int i = 0; i < count; i++) {
            outQueue.remove();
        }
    }

    private boolean isLegalXML10Char(int num) {
        return ((num == 9 || num == 10 || num == 13 ||
                ((num >= 32) && (num <= 55295)) ||
                ((num >= 57344) && num <= 65533)) ||
                ((num >= 10000) && (num <= 1114111)));
    }

    // The format of a character reference is:
    // &#xn; where n is a number in hex of maximum 3 digits
    // &#n; where n is a number in decimal of maximum 3 digits
    // The function reads a byte to outQueue. If it looks like a character reference, read ahead till all character
    // reference bytes are received and if it's not a valid XML 1.0 character, remove the bytes.
    // outQueue will contain the characters that were validated.
    private void readAhead(StringBuffer stringBuffer) throws IOException {
        // Work up some look-ahead.
        int next = super.read();
        outQueue.offer(next);
        stringBuffer.append((char)next);

        switch(next) {
            case -1:
                break;
            case '&':
                if (stringBuffer.length() == 1) {
                    readAhead(stringBuffer);
                }
                break;
            case '#':
                if (stringBuffer.length() == 2) {
                    readAhead(stringBuffer);
                }
                break;
            case 'x':
                if (stringBuffer.length() == 3) {
                    readAhead(stringBuffer);
                }
                break;
            case ';':
                if (stringBuffer.length() > 4 && stringBuffer.charAt(2) == 'x') {
                    // If the case is &#xn; (hex)
                    if (removeIllegalBytesIfNeeded(stringBuffer, true)) {
                        readAhead(new StringBuffer());
                    } else {
                        break;
                    }
                } else if (stringBuffer.length() > 3) {
                    // If the case is &#n; (decimal)
                    if (removeIllegalBytesIfNeeded(stringBuffer, false)) {
                        readAhead(new StringBuffer());
                    } else {
                        break;
                    }
                }
                break;
            default:
                // In this case we got a regular byte that isn't a part of the format -
                // There is no '&#' at the beginning or there is no ';' at the end.
                if ((stringBuffer.length() < 3) || (stringBuffer.length() > 6)) {
                    break;
                }

                // In this case we got a regular byte that is a part of the format, if it's a decimal/hex digit, continue reading.
                if ((char)next >= '0' && (char)next <= '9') {
                    readAhead(stringBuffer);
                } else if (stringBuffer.charAt(2) == 'x' &&
                        (((char)next >= 'a' && (char)next <= 'f') ||
                                ((char)next >= 'A' && (char)next <= 'F'))) {
                    readAhead(stringBuffer);
                }

                break;
        }
    }

    @Override
    public int read() throws IOException {
        // outQueue contains bytes that were already validated.
        if (outQueue.isEmpty()) {
            readAhead(new StringBuffer());
        }

        return outQueue.remove();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        // These validations are part of InputStream specification
        if (b == null) {
            throw new NullPointerException();
        }
        if (len == 0) {
            return 0;
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        int readBytesCount = 0;
        for ( ; readBytesCount < len; readBytesCount++) {
            int byteRead;
            try {
                byteRead = (byte) read();
            } catch (IOException e) {
                // If thrown at the first call, throw again. Otherwise, threat as if it were end of file - specification.
                if (readBytesCount == 0) {
                    throw e;
                }

                break;
            }

            if (byteRead == -1) {
                if (readBytesCount == 0) {
                    return -1;
                } else {
                    return readBytesCount;
                }
            }

            b[off + readBytesCount] = (byte) byteRead;
        }

        return readBytesCount;
    }
}