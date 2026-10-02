package playbook.encore.back.allowip.util;

import java.net.InetAddress;

/**
 * 단일 IP / CIDR 대역 표기를 파싱하고 매칭하는 불변 매처.
 *
 * <p>DNS 조회를 절대 하지 않는다. 리터럴이 아닌 문자열(호스트명 등)은 전부 파싱 실패로 처리한다.
 * IPv4-mapped IPv6(::ffff:192.168.0.1)는 4바이트 IPv4 로 정규화해 비교한다.
 */
public final class IpRangeMatcher {

    public static final String TYPE_SINGLE = "SINGLE";
    public static final String TYPE_CIDR = "CIDR";

    /** 호스트 비트를 0으로 깎은 네트워크 주소 (4 또는 16바이트) */
    private final byte[] network;
    /** 프리픽스 길이(비트) */
    private final int prefixBits;

    private IpRangeMatcher(byte[] network, int prefixBits) {
        this.network = network;
        this.prefixBits = prefixBits;
    }

    // ===== 파싱 =====

    /**
     * "192.168.0.1" / "192.168.0.0/24" / "2001:db8::/32" 를 매처로 변환한다.
     *
     * @return 표기가 유효하지 않으면 null
     */
    public static IpRangeMatcher parse(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 64) {
            return null;
        }

        int slash = value.indexOf('/');
        if (slash < 0) {
            byte[] addr = parseAddress(value);
            if (addr == null) {
                return null;
            }
            return new IpRangeMatcher(addr, addr.length * 8);
        }

        byte[] addr = parseAddress(value.substring(0, slash));
        if (addr == null) {
            return null;
        }
        String prefixPart = value.substring(slash + 1);
        if (prefixPart.isEmpty() || prefixPart.length() > 3) {
            return null;
        }
        for (int i = 0; i < prefixPart.length(); i++) {
            char c = prefixPart.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        int prefix = Integer.parseInt(prefixPart);
        int max = addr.length * 8;
        if (prefix > max) {
            return null;
        }
        return new IpRangeMatcher(maskOff(addr, prefix), prefix);
    }

    /**
     * IP 리터럴을 바이트 배열로 변환한다 (DNS 조회 없음).
     *
     * @return 리터럴이 아니면 null
     */
    public static byte[] parseAddress(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty() || s.length() > 64) {
            return null;
        }
        // 대괄호 표기 [::1] 허용
        if (s.length() > 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
            s = s.substring(1, s.length() - 1);
        }
        if (s.indexOf(':') >= 0) {
            // IPv6 후보: 16진수·콜론·점만 허용해 호스트명이 DNS 조회로 새는 것을 막는다
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                boolean ok = (c >= '0' && c <= '9')
                        || (c >= 'a' && c <= 'f')
                        || (c >= 'A' && c <= 'F')
                        || c == ':' || c == '.';
                if (!ok) {
                    return null;
                }
            }
            try {
                return normalize(InetAddress.getByName(s).getAddress());
            } catch (Exception e) {
                return null;
            }
        }
        return parseIpv4(s);
    }

    /** 엄격한 점 4구획 IPv4 파서 (선행 0·8진수·부분표기 모두 거부) */
    private static byte[] parseIpv4(String s) {
        byte[] out = new byte[4];
        int octet = 0;
        int value = 0;
        int digits = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (digits == 0 || octet >= 3) {
                    return null;
                }
                out[octet++] = (byte) value;
                value = 0;
                digits = 0;
            } else if (c >= '0' && c <= '9') {
                if (++digits > 3) {
                    return null;
                }
                value = value * 10 + (c - '0');
                if (value > 255) {
                    return null;
                }
            } else {
                return null;
            }
        }
        if (digits == 0 || octet != 3) {
            return null;
        }
        out[3] = (byte) value;
        return out;
    }

    public static boolean isValidLiteral(String s) {
        return parseAddress(s) != null;
    }

    /** ipValue 표기로부터 SINGLE / CIDR 을 판정한다. */
    public static String resolveType(String ipValue) {
        return (ipValue != null && ipValue.indexOf('/') >= 0) ? TYPE_CIDR : TYPE_SINGLE;
    }

    /** 루프백 여부 (127.0.0.0/8, ::1) */
    public static boolean isLoopback(String ip) {
        byte[] addr = parseAddress(ip);
        if (addr == null) {
            return false;
        }
        try {
            return InetAddress.getByAddress(addr).isLoopbackAddress();
        } catch (Exception e) {
            return false;
        }
    }

    /** 등록 폼 기본값으로 쓸 제안 대역 (IPv4 /24, IPv6 /64). 파싱 실패 시 null */
    public static String suggestCidr(String ip) {
        byte[] addr = parseAddress(ip);
        if (addr == null) {
            return null;
        }
        int prefix = addr.length == 4 ? 24 : 64;
        byte[] net = maskOff(addr, prefix);
        try {
            return InetAddress.getByAddress(net).getHostAddress() + "/" + prefix;
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 매칭 =====

    public boolean matches(String ip) {
        byte[] addr = parseAddress(ip);
        return addr != null && matches(addr);
    }

    public boolean matches(byte[] rawAddr) {
        if (rawAddr == null) {
            return false;
        }
        byte[] addr = normalize(rawAddr);
        if (addr.length != network.length) {
            return false;
        }
        int fullBytes = prefixBits / 8;
        int remBits = prefixBits % 8;
        for (int i = 0; i < fullBytes; i++) {
            if (addr[i] != network[i]) {
                return false;
            }
        }
        if (remBits > 0) {
            int mask = (0xFF << (8 - remBits)) & 0xFF;
            return (addr[fullBytes] & mask) == (network[fullBytes] & mask);
        }
        return true;
    }

    /**
     * 두 대역이 겹치는지 판정한다.
     * CIDR 블록은 서로 disjoint 이거나 한쪽이 다른 쪽을 포함하는 관계뿐이므로,
     * 상대의 네트워크 주소가 내 대역에 들어오는지(또는 그 반대)만 보면 정확하다.
     */
    public boolean overlaps(IpRangeMatcher other) {
        if (other == null) {
            return false;
        }
        return this.matches(other.network) || other.matches(this.network);
    }

    @Override
    public String toString() {
        try {
            return InetAddress.getByAddress(network).getHostAddress() + "/" + prefixBits;
        } catch (Exception e) {
            return "invalid";
        }
    }

    // ===== 내부 =====

    /** IPv4-mapped IPv6(::ffff:a.b.c.d)를 4바이트로 줄인다. */
    private static byte[] normalize(byte[] addr) {
        if (addr.length != 16) {
            return addr;
        }
        for (int i = 0; i < 10; i++) {
            if (addr[i] != 0) {
                return addr;
            }
        }
        if ((addr[10] & 0xFF) != 0xFF || (addr[11] & 0xFF) != 0xFF) {
            return addr;
        }
        return new byte[]{addr[12], addr[13], addr[14], addr[15]};
    }

    private static byte[] maskOff(byte[] addr, int prefixBits) {
        byte[] out = addr.clone();
        for (int i = 0; i < out.length; i++) {
            int bitStart = i * 8;
            if (bitStart >= prefixBits) {
                out[i] = 0;
            } else if (bitStart + 8 > prefixBits) {
                int remBits = prefixBits - bitStart;
                out[i] = (byte) (out[i] & ((0xFF << (8 - remBits)) & 0xFF));
            }
        }
        return out;
    }

    /** 쉼표 구분 CIDR 목록을 파싱한다. 유효하지 않은 항목은 건너뛴다. */
    public static java.util.List<IpRangeMatcher> parseList(String csv) {
        java.util.List<IpRangeMatcher> out = new java.util.ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return out;
        }
        for (String token : csv.split(",")) {
            IpRangeMatcher m = parse(token);
            if (m != null) {
                out.add(m);
            }
        }
        return out;
    }

    public static boolean matchesAny(java.util.List<IpRangeMatcher> matchers, String ip) {
        if (matchers == null || matchers.isEmpty()) {
            return false;
        }
        byte[] addr = parseAddress(ip);
        if (addr == null) {
            return false;
        }
        for (IpRangeMatcher m : matchers) {
            if (m.matches(addr)) {
                return true;
            }
        }
        return false;
    }
}
