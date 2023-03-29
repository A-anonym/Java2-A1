import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower
 * version). This is just a demo, and you can extend and implement functions based on this demo, or
 * implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]),
            Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]),
            Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]),
            Double.parseDouble(info[20]),
            Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //1
  public Map<String, Integer> getPtcpCountByInst() {
    Stream<Course> stream = courses.stream();
    Map<String, Integer> map = new HashMap();
    map = stream.sorted(Comparator.comparing(Course::getInstitution).reversed()).collect(
        Collectors.groupingBy(Course::getInstitution,
            Collectors.summingInt(Course::getParticipants)));
//        map.forEach((k, v) -> System.out.println(k + "=" + v));
    return map;
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Stream<Course> stream = courses.stream();
    Map<String, Integer> map = new HashMap<>();
    map = stream.collect(Collectors.groupingBy(Course::getInstAndSubject,
        Collectors.summingInt(Course::getParticipants)));
    Map<String, Integer> sortedMap = map.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.<String, Integer>comparingByValue()
            .thenComparing(Map.Entry.comparingByKey())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//        Stream<Map.Entry<String, Integer>> entrySet=map.entrySet().stream();
//        sortedMap.forEach((k, v) -> System.out.println(k + "=" + v));
    return sortedMap;
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Stream<Course> stream = courses.stream();
    Map<String, List<List<String>>> map = new HashMap<>();
    Map<String, Set<String>> independentlyResponsibleCourses = new HashMap<>();
    Map<String, Set<String>> coDevelopedCourses = new HashMap<>();
    for (Course course : courses) {
      String[] instructors = course.getInstructors().split(",");
      String courseTitle = course.getTitle();
      for (String s : instructors) {
        if (!independentlyResponsibleCourses.containsKey(s.trim())) {
          independentlyResponsibleCourses.put(s.trim(), new TreeSet<>());
        }
        if (!coDevelopedCourses.containsKey(s.trim())) {
          coDevelopedCourses.put(s.trim(), new TreeSet<>());
        }
      }
      for (String s : instructors) {
        if (instructors.length == 1) {
          independentlyResponsibleCourses.get(s.trim()).add(courseTitle);
        } else {
          coDevelopedCourses.get(s.trim()).add(courseTitle);
        }
//                if (s.equals(" John Guttag")) {
//                    System.out.println(s);
//                    System.out.println(independentlyResponsibleCourses.get(s));
//                    System.out.println(coDevelopedCourses.get(s));
//                }
      }
    }

    for (String instructor : independentlyResponsibleCourses.keySet()) {
      List<List<String>> instructorList = new ArrayList<>();
      Set<String> independentCourses = independentlyResponsibleCourses.get(instructor);
      Set<String> coDeveloped = coDevelopedCourses.get(instructor);

      // Add the independent courses to the instructorList
      List<String> independentList = new ArrayList<>(independentCourses);
      Collections.sort(independentList);

      instructorList.add(independentList);

      // Add the co-developed courses to the instructorList
      List<String> coDevelopedList = new ArrayList<>(coDeveloped);
      Collections.sort(coDevelopedList);
      instructorList.add(coDevelopedList);

      // Add the instructorList to the final map
      map.put(instructor, instructorList);
    }
//        map.forEach((key, value) -> {
//            System.out.println("Key: " + key);
//            System.out.println("Values: " + value);
//        });
    return map;
  }


  //4
  public List<String> getCourses(int topK, String by) {
    Stream<Course> stream = courses.stream();
    Comparator<Course> comparator = null;
    if (by.equals("hours")) {
      comparator = Comparator.comparing(Course::getTotalHours).reversed()
          .thenComparing(Comparator.comparing(Course::getTitle));
    } else if (by.equals("participants")) {
      comparator = Comparator.comparing(Course::getParticipants).reversed()
          .thenComparing(Comparator.comparing(Course::getTitle));
    }
    List<String> list = new ArrayList<>();
    list = stream.sorted(comparator).map(Course::getTitle).distinct().limit(topK)
        .collect(Collectors.toList());
//        for (String str : list) {
//                System.out.println(str);
//        }
    return list;
  }

  //5
  public List<String> searchCourses(String courseSubject, double percentAudited,
      double totalCourseHours) {
    Stream<Course> stream = courses.stream();
    List<String> list = new ArrayList<>();
    stream = stream.filter(c -> c.getSubject().toLowerCase().contains(courseSubject.toLowerCase()));
    stream = stream.filter(c -> c.getPercentAudited() >= percentAudited);
    stream = stream.filter(c -> c.getTotalHours() <= totalCourseHours);
    stream = stream.sorted(Comparator.comparing(Course::getTitle));
    list = stream.map(Course::getTitle).distinct().collect(Collectors.toList());
    return list;
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Map<String, Date> id_date = new HashMap<>();
    for (Course course : courses) {
      String id = course.getNumber();
      Date date = course.getLaunchDate();
      if (!id_date.containsKey(id)) {
        id_date.put(id, date);
      } else {
        if (date.compareTo(id_date.get(id)) > 0) {
          id_date.put(id, date);
        }
      }
    }
    Map<String, String> id_title = new HashMap<>();
    for (Course course : courses) {
      String id = course.getNumber();
      Date date = course.getLaunchDate();
      String title = course.getTitle();
      if (!id_title.containsKey(id)) {
        id_title.put(id, title);
      } else {
        if (date.compareTo(id_date.get(id)) == 0) {
          id_title.put(id, title);
        }
      }
    }
    Stream<Course> stream = courses.stream();
    Map<String, List<Course>> groupedByid = stream.collect(
        Collectors.groupingBy(Course::getNumber));
    Map<String, List<Double>> avgmap = new HashMap<>();
    groupedByid.forEach((id, ccList) -> {
      double aSum = ccList.stream().mapToDouble(Course::getMedianAge).sum();
      double bSum = ccList.stream().mapToDouble(Course::getPercentMale).sum();
      double cSum = ccList.stream().mapToDouble(Course::getPercentDegree).sum();
      double aAvg = aSum / ccList.size();
      double bAvg = bSum / ccList.size();
      double cAvg = cSum / ccList.size();
      List<Double> l = new ArrayList<>();
      l.add(aAvg);
      l.add(bAvg);
      l.add(cAvg);
      avgmap.put(id, l);
    });

    Map<String, Double> idmap = new HashMap<>();
    avgmap.forEach((id, doublelist) -> {
      //$similarity value= (age -average Median Age)^2 + (gender100 - average Male)^2 + (isBachelorOrHigher100- average Bachelor's Degree or Higher)^2$
//            double similarity=Math.pow((age-doublelist.get(0)),2)+Math.pow((gender*100-doublelist.get(1)),2)+Math.pow((isBachelorOrHigher*100-doublelist.get(2)),2);
      double similarity = (age - doublelist.get(0)) * (age - doublelist.get(0))
          + (gender * 100 - doublelist.get(1)) * (gender * 100 - doublelist.get(1))
          + (isBachelorOrHigher * 100 - doublelist.get(2)) * (isBachelorOrHigher * 100
          - doublelist.get(2));
      idmap.put(id, similarity);
    });
//        idmap.forEach((key, value) -> {
//            System.out.println("Key: " + key);
//            System.out.println("Values: " + value);
//        });
    Map<String, Double> title_sim = new HashMap<>();
    idmap.forEach((id, similaraity) -> {
      String t = id_title.get(id);
      title_sim.put(t, similaraity);
    });
//        System.out.println(idmap);
//        System.out.println(title_sim);
    Map<String, Double> sortedMap = title_sim.entrySet().stream()
        .sorted(
            Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//        System.out.println();
//        sortedMap.forEach((key, value) -> {
//            System.out.println("Key: " + key);
//            System.out.println("Values: " + value);
//        });

    List<Map.Entry<String, Double>> listvalue = new ArrayList<>(sortedMap.entrySet());
//        Set<String> set=new HashSet<>() ;
    List<String> list = new ArrayList<>();
    for (int i = 0; i < listvalue.size(); i++) {
      if (!list.contains(listvalue.get(i).getKey())) {
        list.add(listvalue.get(i).getKey());
      }
//            if (list.size()== 10) {
//                break;
//            }
    }
    list = list.stream().limit(10).collect(Collectors.toList());
    return list;
  }

}

class Course {

  String institution;
  String number;
  Date launchDate;
  String title;
  String instructors;
  String subject;
  int year;
  int honorCode;
  int participants;
  int audited;
  int certified;
  double percentAudited;
  double percentCertified;
  double percentCertified50;
  double percentVideo;
  double percentForum;
  double gradeHigherZero;
  double totalHours;
  double medianHoursCertification;
  double medianAge;
  double percentMale;
  double percentFemale;
  double percentDegree;

  public Course(String institution, String number, Date launchDate,
      String title, String instructors, String subject,
      int year, int honorCode, int participants,
      int audited, int certified, double percentAudited,
      double percentCertified, double percentCertified50,
      double percentVideo, double percentForum, double gradeHigherZero,
      double totalHours, double medianHoursCertification,
      double medianAge, double percentMale, double percentFemale,
      double percentDegree) {
    this.institution = institution;
    this.number = number;
    this.launchDate = launchDate;
      if (title.startsWith("\"")) {
          title = title.substring(1);
      }
      if (title.endsWith("\"")) {
          title = title.substring(0, title.length() - 1);
      }
    this.title = title;
      if (instructors.startsWith("\"")) {
          instructors = instructors.substring(1);
      }
      if (instructors.endsWith("\"")) {
          instructors = instructors.substring(0, instructors.length() - 1);
      }
    this.instructors = instructors;
      if (subject.startsWith("\"")) {
          subject = subject.substring(1);
      }
      if (subject.endsWith("\"")) {
          subject = subject.substring(0, subject.length() - 1);
      }
    this.subject = subject;
    this.year = year;
    this.honorCode = honorCode;
    this.participants = participants;
    this.audited = audited;
    this.certified = certified;
    this.percentAudited = percentAudited;
    this.percentCertified = percentCertified;
    this.percentCertified50 = percentCertified50;
    this.percentVideo = percentVideo;
    this.percentForum = percentForum;
    this.gradeHigherZero = gradeHigherZero;
    this.totalHours = totalHours;
    this.medianHoursCertification = medianHoursCertification;
    this.medianAge = medianAge;
    this.percentMale = percentMale;
    this.percentFemale = percentFemale;
    this.percentDegree = percentDegree;
  }

  public String getInstitution() {
    return institution;
  }

  public String getNumber() {
    return number;
  }

  public Date getLaunchDate() {
    return launchDate;
  }

  public String getTitle() {
    return title;
  }

  public String getInstructors() {
    return instructors;
  }

  public String getSubject() {
    return subject;
  }

  public int getYear() {
    return year;
  }

  public int getHonorCode() {
    return honorCode;
  }

  public int getParticipants() {
    return participants;
  }

  public int getAudited() {
    return audited;
  }

  public int getCertified() {
    return certified;
  }

  public double getPercentAudited() {
    return percentAudited;
  }

  public double getPercentCertified() {
    return percentCertified;
  }

  public double getPercentCertified50() {
    return percentCertified50;
  }

  public double getPercentVideo() {
    return percentVideo;
  }

  public double getPercentForum() {
    return percentForum;
  }

  public double getGradeHigherZero() {
    return gradeHigherZero;
  }

  public double getTotalHours() {
    return totalHours;
  }

  public double getMedianHoursCertification() {
    return medianHoursCertification;
  }

  public double getMedianAge() {
    return medianAge;
  }

  public double getPercentMale() {
    return percentMale;
  }

  public double getPercentFemale() {
    return percentFemale;
  }

  public double getPercentDegree() {
    return percentDegree;
  }

  public String getInstAndSubject() {
    return institution + "-" + subject;
  }

}