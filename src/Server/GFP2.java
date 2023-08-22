package Server;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Random;

public class GFP2 {
    BigInteger p, a, b;

    private static final Random randomizer = new Random(LocalDateTime.now().getNano());

    public GFP2(BigInteger p) {
        this.p = p;

        do {
            this.a = new BigInteger(this.p.bitCount(), randomizer).mod(this.p);
            this.b = new BigInteger(this.p.bitCount(), randomizer).mod(this.p);
        } while (a.equals(b));
    }

    public GFP2(BigInteger p, BigInteger value) {
        this.p = p;
        a = value.negate().mod(p);
        b = a;
    }

    public GFP2(BigInteger p, BigInteger a, BigInteger b) {
        this.p = p;
        this.a = a.mod(p);
        this.b = b.mod(p);
    }

    public GFP2 getSwaped() {
        return new GFP2(this.p, this.b, this.a);
    }

    public GFP2 getSquared() {
        return new GFP2(this.p, this.b.multiply(this.b.subtract(this.a).subtract(this.a)),
                this.a.multiply(this.a.subtract(this.b).subtract(this.b)));
    }

    public boolean myEquals(GFP2 other) {
        return this.a.equals(other.a) && this.b.equals(other.b);
    }

    public boolean isGFP() {
        return this.a.equals(this.b);
    }

    public GFP2 add(GFP2 other) {
        return new GFP2(this.p, this.a.add(other.a), this.b.add(other.b));
    }

    public GFP2 sub(GFP2 other) {
        return new GFP2(this.p, this.a.subtract(other.a), this.b.subtract(other.b));
    }

    public static GFP2 specOp(GFP2 x, GFP2 y, GFP2 z) {
        return new GFP2(x.p, z.a.multiply(y.a.subtract(x.b).subtract(y.b)).add(z.b.multiply(x.b.subtract(x.a).add(y.b))),
                z.a.multiply(x.a.subtract(x.b).add(y.a)).add(z.b.multiply(y.b.subtract(x.a).subtract(y.a))));
    }

    public GFP2 copy() {
        return new GFP2(this.p, this.a, this.b);
    }

    public BigInteger toBigInteger() {
        return this.a.add(this.b.multiply(p));
    }

}
