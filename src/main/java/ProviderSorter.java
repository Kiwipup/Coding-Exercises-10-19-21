import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import sun.misc.ClassLoaderUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProviderSorter {

    public static void main(String[] args) throws FileNotFoundException {
        String providerCsv = "provider.csv";
        List<Provider> providerList = readProvidersFromCsv(providerCsv);
        sort(providerList);
    }

    private static List<Provider> readProvidersFromCsv(String providerCsv) throws FileNotFoundException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(providerCsv);
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Reader reader = new BufferedReader(streamReader);
        CsvToBean<Provider> csvReader = new CsvToBeanBuilder(reader)
                .withType(Provider.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();

        return csvReader.parse();
    }

    private static void sort(List<Provider> providerList) {
        Map<String, List<Provider>> grouped = groupByInsuranceCompany(providerList);
        sortByFirstAndLastName(grouped);
    }

    private static void sortByFirstAndLastName(Map<String, List<Provider>> grouped) {
        grouped.values().forEach(list -> {
            list.sort(Comparator.comparing(Provider::getFirstName).thenComparing(Provider::getLastName));
        });
    }

    private static Map<String,List<Provider>> groupByInsuranceCompany(List<Provider> providerList) {
        Map<String, List<Provider>> grouped = new HashMap<>();
        for (Provider p : providerList) {
            List<Provider> insCompanyProviders = grouped.computeIfAbsent(p.getInsuranceCompany(), (key) -> new ArrayList<>());
            insCompanyProviders.add(p);
        }
        return grouped;
    }

}
