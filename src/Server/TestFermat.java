package Server;

import java.math.BigInteger;
import java.util.Random;

public class TestFermat implements PrimeNumberTest {
    @Override
    public boolean check(BigInteger n, int confidence) {
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3)))
            return true;


        Random random = new Random();

        for (int i = 0; i < confidence; i++) {
            BigInteger a = new BigInteger(n.bitLength(), random);
            //выбираем число до n - 1
            a = a.mod(n.subtract(BigInteger.TWO)).add(BigInteger.TWO);

            if (!witness(a, n)) {
                return false;
            }
        }

        return true;
    }

    private static boolean witness(BigInteger a, BigInteger number) {
        BigInteger result = a.modPow(number.subtract(BigInteger.ONE), number);

        return result.equals(BigInteger.ONE);
    }

}
