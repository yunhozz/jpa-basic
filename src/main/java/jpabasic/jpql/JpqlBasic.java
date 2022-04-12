package jpabasic.jpql;

import jpabasic.Address;

import javax.persistence.*;

public class JpqlBasic {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        /**
         * JPQL 기본 문법
         */
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        try {
            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);

            em1.persist(user);
            em1.flush();
            em1.clear();

            /*
            TypedQuery : 반환 타입이 명확할 때 사용
            Query : 반환 타입이 명확하지 않을 때 사용
             */
            TypedQuery<User> userQuery = em1.createQuery("select u from User u", User.class);
            TypedQuery<String> stringQuery = em1.createQuery("select u.userName from User u", String.class);
            Query query = em1.createQuery("select u.userName, u.age from User u");

            /*
            query.getResultList() : 결과가 하나 이상일 때 리스트 반환, 결과가 없으면 빈 리스트 반환
            query.getSingleResult() : 결과가 정확히 하나일 때 단일 객체 반환
                                       -> 결과가 없으면 NoResultException, 둘 이상이면 NonUniqueResultException
             */

            /*
            파라미터 바인딩 - 이름 기준, 위치 기준(왠만하면 사용 x)
             */
            em1.createQuery("select u from User u where u.userName = :userName", User.class)
                    .setParameter("userName", "yunho")
                    .getSingleResult();

            tx1.commit();

        } catch (Exception e) {
            tx1.rollback();
            e.printStackTrace();

        } finally {
            em1.close();
        }

        System.out.println("=============================================");

        /**
         * 프로젝션(SELECT) -> 영속성 컨텍스트에 의해 전부 관리됨
         */
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);

            em2.persist(user);
            em2.flush();
            em2.clear();

            //엔티티 프로젝션
            em2.createQuery("select u from User u", User.class).getResultList();
            em2.createQuery("select u.team from User u", Team.class).getResultList(); //이렇게 사용x, 왠만하면 SQL 과 비슷하게 써야함
//            em2.createQuery("select t from User u join u.team t", Team.class).getResultList();

            //임베디드 타입 프로젝션
            em2.createQuery("select o.address from Order o", Address.class).getResultList();

            //스칼라 타입 프로젝션 : Query 타입으로 조회, Object[] 타입으로 조회, new 명령어로 조회(Dto 로 바로 조회)
            em2.createQuery("select distinct u.userName, u.age from User u").getResultList(); //query
            em2.createQuery("select new jpabasic.jpql.UserDto(u.userName, u.age) from User u", UserDto.class)
                    .getResultList(); //new dto

            tx2.commit();

        } catch (Exception e) {
            tx2.rollback();
            e.printStackTrace();

        } finally {
            em2.close();
        }

        System.out.println("=============================================");

        /**
         * 페이징 : setFirstResult(조회 시작 위치), setMaxResults(조회할 데이터 수)
         */
        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();

        try {
            for (int i = 0; i < 3; i++) {
                User user = new User();
                user.setUserName("yunho_" + i);
                user.setAge(i);
                em3.persist(user);
            }

            em3.flush();
            em3.clear();

            //1번째 부터 2개 데이터 조회 -> yunho_1, yunho_2
            em3.createQuery("select u from User u order by u.age desc", User.class) //asc : 오름차순 desc : 내림차순
                    .setFirstResult(1) //offset = 1
                    .setMaxResults(2) //limit = 2
                    .getResultList();

            tx3.commit();

        } catch (Exception e) {
            tx3.rollback();
            e.printStackTrace();

        } finally {
            em3.close();
        }

        System.out.println("=============================================");

        /**
         * 조인 -> 내부 조인(inner), 외부 조인(left outer), 세타 조인(cross)
         */
        EntityManager em4 = emf.createEntityManager();
        EntityTransaction tx4 = em4.getTransaction();
        tx4.begin();

        try {
            Team team = new Team();
            team.setName("teamA");

            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);
            user.changeTeam(team);

            em4.persist(team);
            em4.persist(user);
            em4.flush();
            em4.clear();

            //내부 조인 : inner join (inner 생략 가능)
            em4.createQuery("select u from User u join u.team t", User.class)
                    .getResultList();

            //외부 조인 : left outer join (outer 생략 가능)
            em4.createQuery("select u from User u left join u.team t", User.class)
                    .getResultList();

            //세타 조인 -> cross
            em4.createQuery("select u from User u, Team t where u.userName = t.name", User.class)
                    .getResultList();

            //on 절을 활용한 조인 (JPA 2.1부터 지원)

            //조인 대상 필터링(예시 : 팀 이름이 A 인 팀과 회원을 조인)
            em4.createQuery("select u from User u join u.team t on t.name = 'A'", User.class)
                    .getResultList();

            //연관관계가 없는 엔티티 외부 조인(예시 : 회원의 이름과 팀의 이름이 같은 대상 외부 조인)
            em4.createQuery("select u from User u left join Team t on u.userName = t.name", User.class)
                    .getResultList();

//            em4.createQuery("select u from User u left join Team t where u.userName = t.name", User.class)
//                    .getResultList();

            /*
            on, where 차이 : 조건식을 언제 검사하느냐의 차이가 있다. (내부 조인은 상관 x)
            on 절을 사용할 때 -> 조인을 통해 결과 테이블을 찾을 때 해당 조건을 검색 (조건을 만족하지 않으면 null 로 존재)
            where 절을 사용할 때 -> 일단 left join 으로 결과 테이블을 가져온 후 해당 조건 검색 (null 존재 x)
             */

            tx4.commit();

        } catch (Exception e) {
            tx4.rollback();
            e.printStackTrace();

        } finally {
            em4.close();
        }

        System.out.println("=============================================");

        /**
         * 서브 쿼리 : (not) exists, all, any, some, (not) in -> true, false 반환 / where, having 절에서만 사용 가능
         */
        EntityManager em5 = emf.createEntityManager();
        EntityTransaction tx5 = em5.getTransaction();
        tx5.begin();

        try {
            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);

            em5.persist(user);
            em5.flush();
            em5.clear();

            //나이가 평균보다 많은 회원
            em5.createQuery("select u from User u where u.age > (select avg(u2.age) from User u2)", User.class)
                    .getResultList();

            //한 건이라도 주문한 고객
//            em5.createQuery("select u from User u where (select o from Order o where o.user = u) > 0", User.class)
//                    .getResultList();

            //팀 'A' 소속인 회원
            em5.createQuery("select u from User u where exists (select t from u.team t where t.name = 'A')", User.class)
                    .getResultList();

            //전체 상품 각각의 재고보다 주문량이 많은 주문들
//            em5.createQuery("select o from Order o where o.orderAmount > all (select p.stockAmount from Product p)", Order.class)
//                    .getResultList();

            //어떤 팀이든 팀에 소속된 회원
            em5.createQuery("select u from User u where u.team = any (select t from Team t)", User.class)
                    .getResultList();

            tx5.commit();

        } catch (Exception e) {
            tx5.rollback();
            e.printStackTrace();

        } finally {
            em5.close();
        }

        System.out.println("=============================================");

        /**
         * 조건식(case)
         */
        EntityManager em6 = emf.createEntityManager();
        EntityTransaction tx6 = em6.getTransaction();
        tx6.begin();

        try {
            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);

            em6.persist(user);
            em6.flush();
            em6.clear();

            String query =
                    "select " +
                            "case when m.age <= 10 then '학생요금' " +
                            "     when m.age >= 60 then '경로요금' " +
                            "     else '일반요금' " +
                            "end " +
                    "from Member m";

            em6.createQuery(query).getResultList();

            tx5.commit();

        } catch (Exception e) {
            tx5.rollback();
            e.printStackTrace();

        } finally {
            em5.close();
        }

        /**
         * JPQL 기본 함수 : concat, substring, trim, lower/upper, length, locate, abs, size, ...
         */

        emf.close();
    }
}
