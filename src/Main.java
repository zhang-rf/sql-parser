import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Map<Integer, List<Pattern>> dictionary = new HashMap<>();
        putToDictionary(dictionary, 2, "(INNER|LEFT|RIGHT|FULL) OUTER");
        putToDictionary(dictionary, 2, "(INNER|LEFT|RIGHT|FULL)( OUTER)* JOIN");
        putToDictionary(dictionary, 3, ".+ AS [^ ]+");
        putToDictionary(dictionary, 3, "\\w+ IS NULL");
        putToDictionary(dictionary, 4, "\\w+ IS NOT NULL");
        int max = dictionary.keySet().stream().mapToInt(n -> n).max().orElseThrow(Error::new);

        String sql = "select distinct high_priority a as A, b as B, c, d.*, count(*) as C from my_table , (select * from t_table) as your_table left outer join (select id from jtb) as j on a.id=j.id where a <> b and c is null or not (d is not null and e = '123' and f = '321');";
        List<String> sqlStrings = new ArrayList<>();
        sql = removeSqlStrings(sql, sqlStrings);
        sql = compactSql(sql);
        String[] split = sql.split(" ");

        List<String> list = new ArrayList<>(split.length);
        StringBuilder builder = new StringBuilder();
        for (int index = 0, offset = 0; offset < split.length; ) {
            int count = index - offset + 1;
            List<Pattern> patterns = dictionary.get(count);
            if (patterns != null) {
                boolean matched = false;
                builder.setLength(0);
                for (int i = offset; i <= index; i++) {
                    builder.append(split[i]).append(' ');
                }
                builder.setLength(builder.length() - 1);
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(builder).matches()) {
                        split[index] = builder.toString();
                        offset = index;
                        matched = true;
                        break;
                    }
                }
                if (matched) continue;
            }
            if (count < max && index < split.length - 1) {
                index++;
            } else {
                list.add(split[offset++]);
                index = offset;
            }
        }
        list = postProcess(list);
        System.out.println(list);
        System.out.println(String.format(sql, sqlStrings.toArray()));
    }

    private static List<String> postProcess(List<String> list) {
        List<String> newList = new ArrayList<>(list.size());
        for (String s : list) {
            if (s.startsWith("(")) {
                newList.add("(");
                newList.add(s.substring(1));
            } else if (s.endsWith(")")) {
                newList.add(s.substring(0, s.length() - 1));
                newList.add(")");
            } else {
                newList.add(s);
            }
        }
        return newList;
    }

    private static void putToDictionary(Map<Integer, List<Pattern>> dictionary, int n, String regex) {
        dictionary.compute(n, (k, v) -> {
            if (v == null) {
                return new ArrayList<>(Collections.singleton(Pattern.compile(regex, Pattern.CASE_INSENSITIVE)));
            }
            v.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            return v;
        });
    }

    private static String removeSqlStrings(String sql, List<String> container) {
        StringBuilder builder = new StringBuilder(sql);
        StringIterator iterator = new StringIterator(sql);

        int offset = 0;
        String next;
        while ((next = iterator.next()) != null) {
            container.add(next);
            int index = iterator.index() - (next.length() - 1);
            builder.replace(index - offset, index - offset + next.length(), "%s");
            offset += next.length() - 2;
        }
        return builder.toString();
    }

    private static String compactSql(String sql) {
        sql = sql.replace("\n", " ");
        sql = sql.replaceAll(" {2,}", " ");
        sql = sql.replaceAll("(\\() *", "$1");
        sql = sql.replaceAll(" *(\\))", "$1");
        sql = Pattern.compile(" *([,!=<>+\\-/%]) *").matcher(sql).replaceAll("$1");
        sql = Pattern.compile("(?<!select) *(\\*) *(?!from)", Pattern.CASE_INSENSITIVE).matcher(sql).replaceAll("$1");
        sql = Pattern.compile("(?<!\\w)(\\(select .*?\\))").matcher(sql).replaceAll("#child");
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1);
        return sql;
    }
}
