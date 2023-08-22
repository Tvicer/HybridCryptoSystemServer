package Server;

import java.math.BigInteger;
import java.util.Random;

public class TestSolovayStrassen implements PrimeNumberTest {
    @Override
    public boolean check(BigInteger n, int confidence) {
        if (n.equals(BigInteger.ONE) || n.equals(BigInteger.TWO)) {
            return true;
        }
        if (n.compareTo(BigInteger.TWO) < 0 || n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            return false;
        }


        BigInteger a, r, s;
        Random random = new Random();

        for (int i = 0; i < confidence; i++) {
            a = new BigInteger(n.bitLength() - 1, random).add(BigInteger.ONE);

            if (gcd(a, n).equals(BigInteger.valueOf(1)))
                return false;

            r = a.divide(n).add(BigInteger.ONE);
            s = a.modPow((n.subtract(BigInteger.ONE)).divide(BigInteger.TWO).add(BigInteger.ONE), n);

            if (!a.gcd(n).equals(BigInteger.ONE))
                return false;
        }

        return true;
    }

    public BigInteger gcd(BigInteger a, BigInteger b) {
        if (BigInteger.valueOf(0).equals(b)) return a;
        return gcd(b, a.mod(b));
    }

}
