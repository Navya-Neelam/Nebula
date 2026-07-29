package com.nebula.auth.config;

import com.nebula.auth.model.Course;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.CourseRepository;
import com.nebula.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(CourseRepository courseRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Clear and seed users with specific roles
        userRepository.deleteAll();

        User admin = new User("System Admin", "admin@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
        admin.setRole("ADMIN");
        admin.setVerified(true);
        admin.setActive(true);
        admin.setFirstName("System");
        admin.setLastName("Admin");
        userRepository.save(admin);

        User instructor = new User("Dr. Clara Sterling", "clara@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
        instructor.setRole("INSTRUCTOR");
        instructor.setVerified(true);
        instructor.setActive(true);
        instructor.setFirstName("Clara");
        instructor.setLastName("Sterling");
        User savedInstructor = userRepository.save(instructor);

        User student = new User("Jane Student", "student@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
        student.setRole("STUDENT");
        student.setVerified(true);
        student.setActive(true);
        student.setFirstName("Jane");
        student.setLastName("Student");
        userRepository.save(student);

        // Clear and re-seed to ensure all courses contain the new price field and backend courses
        courseRepository.deleteAll();

        List<Course> courses = Arrays.asList(
            new Course(
                "Introduction to Generative AI",
                "Learn the fundamentals of Generative AI, Large Language Models (LLMs), and prompt engineering.",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "6 hours",
                4.8,
                "Dr. Clara Sterling",
                "This course provides a comprehensive introduction to Generative AI. You will learn the history of transformer models, prompt engineering patterns, fine-tuning techniques, and how to build applications using LLM APIs. Ideal for beginners and professionals wanting to stay ahead in tech.",
                49.99
            ),
            new Course(
                "Advanced Prompt Engineering",
                "Master complex prompt engineering frameworks and build multi-agent LLM systems.",
                "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "8 hours",
                4.9,
                "Alex Rivera",
                "Unlock the true potential of AI with advanced techniques like Chain-of-Thought, ReAct framing, and Directional Stimulus prompting. You will construct autonomous agents using LangChain and learn practical security measures against prompt injection.",
                79.99
            ),
            new Course(
                "Data Science & Machine Learning Bootcamp",
                "Go from zero to hero in Python, Pandas, NumPy, Scikit-Learn, and statistics.",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "40 hours",
                4.7,
                "Prof. Marcus Vance",
                "A complete bootcamp covering the full data science lifecycle. Learn data visualization, exploratory analysis, linear regression, decision trees, random forests, and unsupervised learning algorithms. Includes hands-on projects with real-world datasets.",
                149.99
            ),
            new Course(
                "Deep Learning & Neural Networks",
                "Understand CNNs, RNNs, and Transformers using PyTorch and TensorFlow.",
                "https://images.unsplash.com/photo-1527474305487-b87b222841cc?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "25 hours",
                4.8,
                "Dr. Sophia Chen",
                "Delve deep into neural network architectures. Learn backpropagation, activation functions, convolutional layers for computer vision, recurrent structures for sequence processing, and the revolutionary attention mechanism that powers today's LLMs.",
                119.99
            ),
            new Course(
                "Introduction to SQL & Database Design",
                "Master PostgreSQL, relational database architecture, and complex subqueries.",
                "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "14 hours",
                4.7,
                "Colt Steele",
                "Learn relational design principles, indexing optimizations, and query writing. Write complex inner/outer joins, aggregate analytical operations, subqueries, trigger statements, and window mathematical functions.",
                39.99
            ),
            new Course(
                "Angular 18 Complete Guide",
                "Master standalone components, signals, routing, and RxJS in modern Angular.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "30 hours",
                4.8,
                "Maximilian Schwarz",
                "Build performant enterprise web applications using modern Angular. Learn the new Signals state management model, standalone component architectures, advanced routing strategies, and complex RxJS data streams.",
                99.99
            ),
            new Course(
                "Full-Stack Spring Boot & React",
                "Connect Spring Boot REST APIs with a modern React frontend using Spring Security.",
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "35 hours",
                4.9,
                "John Thompson",
                "Develop a complete production-ready application. Build secure Spring Boot REST endpoints with JWT validation, connect to a PostgreSQL database, and manage front-end authentication states seamlessly using React Context and Axios.",
                129.99
            ),
            new Course(
                "Figma UI/UX Design Essentials",
                "Learn user research, wireframing, prototyping, and design systems in Figma.",
                "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "20 hours",
                4.8,
                "Dan Walter",
                "Design high-fidelity interactive user interfaces and build robust user experience maps. Create nested layout components, configure responsive grids, run micro-interactive prototyping flows, and share design handoffs with developers.",
                59.99
            ),
            new Course(
                "Modern JavaScript & TypeScript Masterclass",
                "Master ES6+, asynchronous programming, closures, and TypeScript compiler basics.",
                "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "22 hours",
                4.8,
                "Sarah Drasner",
                "Become a JavaScript expert. Learn advanced concepts like closures, async-await architectures, execution contexts, variable scopes, event loops, and type-safety modeling using TypeScript. Highly recommended for all developers.",
                69.99
            ),
            new Course(
                "Spring Boot Microservices Masterclass",
                "Build scalable backend systems using Spring Cloud, Eureka, Gateway, and Config Server.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "28 hours",
                4.9,
                "Dr. John Doe",
                "Master microservices architecture using Spring Boot and Spring Cloud. Learn service discovery with Eureka, API Gateway routing, centralized configuration, circuit breakers with Resilience4j, and event-driven communication using Kafka.",
                119.99
            ),
            new Course(
                "Node.js Advanced API Design",
                "Learn scalable Express APIs, MongoDB, Redis caching, and WebSocket integrations.",
                "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "24 hours",
                4.8,
                "Sarah Connor",
                "Go deep into Node.js. Build high-performance REST and GraphQL APIs, implement JWT authentication, secure endpoints with rate limiting, cache queries with Redis, and implement real-time communication using WebSockets.",
                89.99
            ),
            new Course(
                "Go (Golang) Backend Essentials",
                "Build highly concurrent microservices with Fiber, Gin, GORM, and Docker.",
                "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "20 hours",
                4.7,
                "Ken Thompson",
                "Learn Google's Go language for backend systems. Harness Go routines and channels for high concurrency, write clean APIs with the Fiber framework, interface with databases using GORM, and write unit and integration tests.",
                99.99
            ),
            new Course(
                "Python Django & FastAPI Bootcamp",
                "Build reactive backends with Django ORM, REST Framework, and asynchronous FastAPI.",
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "32 hours",
                4.8,
                "Guido van Rossum",
                "A comprehensive backend bootcamp using Python. Build relational CRUD systems using Django ORM and Django REST Framework, and develop blazingly fast asynchronous APIs using FastAPI and Pydantic validation.",
                109.99
            ),
            new Course(
                "Docker & Kubernetes for Developers",
                "Learn containerization, scaling, orchestration, and local microservice deployment.",
                "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "15 hours",
                4.6,
                "Nigel Poulton",
                "Understand how to package applications into lightweight containers, manage multi-container systems using Docker Compose, and orchestrate container lifecycles in scale using Kubernetes clusters and Helm charts.",
                59.99
            ),
            new Course(
                "AWS Certified Solutions Architect",
                "Pass the SAA-C03 exam and learn AWS EC2, S3, RDS, Lambda, and VPC inside out.",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "28 hours",
                4.8,
                "Stephane Maarek",
                "Fully prepare for the AWS Solutions Architect Associate exam. Learn cloud architecture best practices, compute hosting options, scalable database configurations, serverless execution models, and secure private virtual networking.",
                129.99
            ),
            new Course(
                "Cybersecurity Fundamentals",
                "Learn cryptography, network defense, threat hunting, and social engineering.",
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "18 hours",
                4.7,
                "Sarah Jenkins",
                "Start your journey in ethical hacking and defense. Understand public-key cryptography, firewalls, intrusion detection systems, malware analysis tools, and the human psychological triggers exploited by social engineering campaigns.",
                69.99
            ),
            new Course(
                "Ethical Hacking: Web Penetration Testing",
                "Discover SQL injection, XSS, CSRF, and session hijacking using Kali Linux.",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "22 hours",
                4.9,
                "Daniel Lowrie",
                "Perform comprehensive web app security audits. Learn to identify OWASP Top 10 vulnerabilities like SQL injection, cross-site scripting (XSS), cross-site request forgery (CSRF), and insecure direct object references using professional pentesting tools.",
                89.99
            ),
            new Course(
                "iOS 18 Swift & SwiftUI Bootcamp",
                "Build beautiful iOS apps using Swift, SwiftUI, SwiftData, and CoreAnimation.",
                "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "32 hours",
                4.8,
                "Angela Yu",
                "Master Apple's modern declarative UI framework. Build complex user interfaces, save persistent local data with SwiftData, integrate native maps, configure notification systems, and prepare your apps for the App Store.",
                99.99
            ),
            new Course(
                "Android 15 Jetpack Compose Guide",
                "Learn Kotlin, Jetpack Compose UI, Clean Architecture, and Coroutines.",
                "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "28 hours",
                4.7,
                "Philipp Lackner",
                "Build native Android applications with Kotlin and modern reactive UI tools. Explore MVVM/MVI architecture, state flows, reactive Compose layouts, work manager, local database access via Room, and test-driven development integrations.",
                89.99
            ),
            new Course(
                "Digital Marketing Masters Class",
                "Master Google Ads, SEO, content strategy, email marketing, and analytics.",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=60",
                "Digital Marketing",
                "12 hours",
                4.6,
                "Ryan Deiss",
                "A comprehensive masterclass on driving web traffic and converting leads. Learn to rank websites using search engine optimization (SEO), construct effective search ad campaigns, write email copy, and read analytics data dashboards.",
                39.99
            ),
            new Course(
                "SEO Strategy & Copywriting",
                "Learn keyword research, on-page optimization, and high-converting writing.",
                "https://images.unsplash.com/photo-1432888622747-4eb9a8efeb07?w=800&auto=format&fit=crop&q=60",
                "Digital Marketing",
                "10 hours",
                4.5,
                "Brian Dean",
                "Boost organic search engine rankings and visitor retention. Learn to perform search intent keyword research, write titles that get clicks, structure layout headings, and build natural backlink relationships.",
                29.99
            )
        );
        User clara = userRepository.findByEmail("clara@nebula.com").orElse(null);
        String claraId = clara != null ? clara.getId() : null;
        for (Course c : courses) {
            c.setInstructorId(claraId);
        }
        courseRepository.saveAll(courses);
    }
}
