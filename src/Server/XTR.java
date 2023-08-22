package Server;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class XTR {
    public PrimeNumberTest mode;
    int keyLength;
    private PublicKey publicKey;
    private BigInteger privateKey;
    private Tracer tracer;
    private GFP2 traceGK;
    private GFP2 traceGB;
    private GFP2 traceGBK;
    private BigInteger b;

    private static Random randomizer = new Random(LocalDateTime.now().getNano());

    public XTR(Entities.TestMode mode, int keyLength) {
        switch (mode) {
            case MILLER_RABIN: {
                this.mode = new TestMillerRabin();
                break;
            }
            case SOLOVEY_STRASSEN: {
                this.mode = new TestSolovayStrassen();
                break;
            }
            default: {
                this.mode = new TestFermat();
                break;
            }
        }
        this.keyLength = keyLength;
        generateXTRKey();

        this.tracer = new Tracer(publicKey.p);

        this.traceGK = tracer.calcTracer(this.privateKey, publicKey.trace);
        BigInteger upperLimit = publicKey.q.subtract(BigInteger.TWO);
        do {
            this.b = new BigInteger(upperLimit.bitLength() - 1, randomizer);
        } while (this.b.compareTo(upperLimit) >= 0 || this.b.compareTo(BigInteger.ONE) <= 0);

        this.traceGB = tracer.calcTracer(b, publicKey.trace);
        this.traceGBK = tracer.calcTracer(b, traceGK);
    }

    public class PublicKey {
        public BigInteger p;
        public BigInteger q;
        public GFP2 trace;
        public GFP2 traceGK;

        public PublicKey(BigInteger p, BigInteger q, GFP2 trace, GFP2 traceGK) {
            this.p = p;
            this.q = q;
            this.trace = trace;
            this.traceGK = traceGK;
        }

    }


    public class Tracer {
        private BigInteger p;
        private GFP2 c = null;
        private HashMap<BigInteger, GFP2> dict_c = new HashMap<>();

        public Tracer(BigInteger p) {
            this.p = p;
        }

        public GFP2 calcTracer(BigInteger n, GFP2 c) {
            if (this.c == null || (this.c != null && !this.c.myEquals(c))) {
                this.c = c;
                this.dict_c.clear();
                dict_c.put(BigInteger.ZERO, new GFP2(p, BigInteger.valueOf(3)));
                dict_c.put(BigInteger.ONE, this.c);
            }
            return getC(n);
        }

        private GFP2 getC(BigInteger n) {
            if (dict_c.containsKey(n)) {
                return dict_c.get(n);
            }

            BigInteger current = BigInteger.ONE;
            int nbits = n.bitLength();
            BigInteger runner = BigInteger.ONE.shiftLeft(nbits - 2);
            for (int i = 1; i < nbits; i++) {
                BigInteger bit = n.and(runner);
                BigInteger newN = current.shiftLeft(1).or(BigInteger.valueOf(bit.equals(BigInteger.ZERO) ? 0 : 1));
                if (!dict_c.containsKey(newN)) {
                    GFP2 currentC = dict_c.get(current);
                    dict_c.put(newN, (!bit.equals(BigInteger.ZERO)) ?
                            GFP2.specOp(getC(current.add(BigInteger.ONE)), this.c, currentC)
                                    .add(getC(current.subtract(BigInteger.ONE)).getSwaped()) :
                            currentC.getSquared().sub(currentC.add(currentC).getSwaped()));
                }
                current = newN;
                runner = runner.shiftRight(1);
            }
            return dict_c.get(n);
        }
    }

    public void generateXTRKey() {
        BigInteger r, q, k, p;
        do {
            r = new BigInteger(keyLength / 2, randomizer);
            q = r.multiply(r).subtract(r).add(BigInteger.ONE);
        } while (!q.mod(BigInteger.valueOf(12)).equals(BigInteger.valueOf(7)) || !mode.check(q, 100));

        do {
            k = new BigInteger(keyLength / 2, randomizer);
            p = r.add(k.multiply(q));
        } while (!mode.check(p, 100) || !p.mod(BigInteger.valueOf(3)).equals(BigInteger.valueOf(2)));

        BigInteger quotient = p.multiply(p).subtract(p).add(BigInteger.ONE).divide(q);

        GFP2 c = new GFP2(p);
        GFP2 three = new GFP2(p, BigInteger.valueOf(3));
        Tracer tracer = new Tracer(p);
        GFP2 tr;

        while (true) {
            GFP2 newC = tracer.calcTracer(p.add(BigInteger.ONE), c);
            if (!newC.isGFP()) {
                tr = tracer.calcTracer(quotient, c);
                if (!tr.myEquals(three)) {
                    break;
                }
            }
            c = new GFP2(p);
        }
        BigInteger secretK = new BigInteger(keyLength / 4, randomizer);
        this.privateKey = secretK;
        GFP2 traceGK = tracer.calcTracer(secretK, tr);

        this.publicKey = new PublicKey(p, q, tr, traceGK);
    }

    public byte[] encryptKey(byte[] key) {
        Tracer tracer = new Tracer(this.publicKey.p);
        GFP2 traceGBK = tracer.calcTracer(this.b, this.publicKey.traceGK);
        BigInteger message = new BigInteger(key);
        //System.out.println("message " + message);
        BigInteger cipher = message.xor(traceGBK.toBigInteger());
        //System.out.println(cipher.bitCount());
        return cipher.toByteArray();
    }

    public byte[] decryptKey(byte[] key) {
        Tracer tracer = new Tracer(this.publicKey.p);
        BigInteger cipher = new BigInteger(key);
        GFP2 decryptKey = tracer.calcTracer(this.privateKey, this.traceGB);
        BigInteger decryptedMsg = decryptKey.toBigInteger().xor(cipher);
        //System.out.println("decryptedMsg " + Arrays.toString(decryptedMsg.toByteArray()));
        return decryptedMsg.toByteArray();
    }

    public void FullTest() {
        if (Optional.ofNullable(publicKey).isEmpty()) {
            generateXTRKey();
        }

        Tracer tracer = new Tracer(publicKey.p);
        //BigInteger secretK = new BigInteger(keyLength / 4, randomizer);
        //System.out.println("K " + secretK);
        GFP2 traceGK = tracer.calcTracer(this.privateKey, publicKey.trace);
        BigInteger b;
        BigInteger upperLimit = publicKey.q.subtract(BigInteger.TWO);
        //System.out.println(" ");
        do {
            b = new BigInteger(upperLimit.bitLength() - 1, randomizer);
        } while (b.compareTo(upperLimit) >= 0 || b.compareTo(BigInteger.ONE) <= 0);
        //System.out.println("Generated b");
        GFP2 traceGB = tracer.calcTracer(b, publicKey.trace);
        GFP2 traceGBK = tracer.calcTracer(b, traceGK);

        BigInteger message = new BigInteger(keyLength / 2, randomizer);
        System.out.println("message " + message);
        BigInteger cipher = message.xor(traceGBK.toBigInteger());
        System.out.println("cipher " + cipher);
        GFP2 decryptKey = tracer.calcTracer(this.privateKey, traceGB);
        //System.out.println("decryptKey " + decryptKey.toBigInteger());
        BigInteger decryptedMsg = decryptKey.toBigInteger().xor(cipher);
        System.out.println("decryptedMsg " + decryptedMsg);
    }

    public BigInteger[] getPublicKey() {
        return new BigInteger[] {this.publicKey.p, this.publicKey.q,
                this.publicKey.trace.a, this.publicKey.trace.b,
                this.publicKey.traceGK.a, this.publicKey.traceGK.b, this.b};
    }
}


