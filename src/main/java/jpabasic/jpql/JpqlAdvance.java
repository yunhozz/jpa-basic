package jpabasic.jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpqlAdvance {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        /**
         * 경로 표현식 : 상태 필드(m.userName), 단일 값 연관 필드, 컬렉션 값 연관 필드
         *      단일 값 연관 필드 -> @ManyToOne, @OneToOne, 대상이 엔티티
         *      컬렉션 값 연관 필드 -> @OneToMany, @ManyToMany, 대상이 컬렉션
         */
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        try {
            Team team = new Team("팀 A");

            User user = new User();
            user.setUserName("yunho");
            user.changeTeam(team);

//            em1.persist(team);
//            em1.persist(user);
//            em1.flush();
//            em1.clear();

            /*
            User - Team
            from 절 뒤에 오는 엔티티 선언 전엔 다른 엔티티와의 연관관계를 모르는 상태 -> 묵시적 내부 조인 발생
            엔티티 선언 후에 비로소 연관관계를 알게 됨 -> 조인 발생 x
             */
            em1.createQuery("select u.userName from User u").getResultList(); //경로 탐색의 끝, 탐색 x
            em1.createQuery("select u.team.name from User u").getResultList(); //묵시적 내부 조인 발생, 탐색 o -> 사용x
            em1.createQuery("select t.users from Team t").getResultList(); //묵시적 내부 조인 발생, 탐색 x -> 사용x

            //이처럼 명시적으로 join 시켜줘야 한다.
            em1.createQuery("select t.name from User u join u.team t").getResultList();
            em1.createQuery("select u from Team t join t.users u").getResultList();

            tx1.commit();

        } catch (Exception e) {
            tx1.rollback();
            e.printStackTrace();

        } finally {
            em1.close();
        }

        System.out.println("=============================================");

        /**
         * 페치 조인(정말 중요!) : 성능 최적화, 하나의 객체로 연관된 엔티티나 컬렉션을 한번에 함께 조회
         */
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            Team team1 = new Team("A");
            Team team2 = new Team("B");

            em2.persist(team1);
            em2.persist(team2);

            User user1 = new User();
            user1.setUserName("yunho1");
            user1.changeTeam(team1);

            User user2 = new User();
            user2.setUserName("yunho2");
            user2.changeTeam(team1);

            User user3 = new User();
            user3.setUserName("yunho3");
            user3.changeTeam(team2);

            em2.persist(user1);
            em2.persist(user2);
            em2.persist(user3);

            em2.flush();
            em2.clear();

            //User(N) : Team(1)

            //엔티티 페치 조인(N:1) -> 즉시 로딩으로 동작하지만 N+1 문제가 발생하지 않는다
            List<User> resultList1 = em2.createQuery("select u from User u join fetch u.team", User.class)
                                        .getResultList();

            //이렇게 작성해도 위와 쿼리는 같지만 결과를 분리해서 받게 된다.
            //반면, fetch join 을 사용하면 객체 하나로 깔끔한 객체 그래프를 받을 수 있다.
            em2.createQuery("select u,t from User u join u.team t").getResultList();

            //컬렉션 페치 조인(1:N) -> 결과가 반복되는 것을 조심해야 한다!! -> "distinct" 사용 (엔티티 중복 제거)
            List<Team> resultList2 = em2.createQuery("select distinct t from Team t join fetch t.users", Team.class)
                                        .getResultList();

            for (User user : resultList1) {
                System.out.println("user = " + user.getUserName() + ", team = " + user.getTeam().getName());
            }

            for (Team team : resultList2) {
                System.out.println("team name = " + team.getName() + ", team = " + team);

                for (User user : team.getUsers()) {
                    System.out.println(" -> user name = " + user.getUserName() + ", user = " + user);
                }
            }

            /*
            페치 조인의 한계 :
                1. 페치 조인 대상에는 별칭을 줄 수 없다. (관례)
                2. 둘 이상의 컬렉션은 페치 조인할 수 없다.
                3. 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다. (N:1, 1:1 은 페이징 가능)
             */

            tx2.commit();

        } catch (Exception e) {
            tx2.rollback();
            e.printStackTrace();

        } finally {
            em2.close();
        }

        System.out.println("=============================================");

        /**
         * 엔티티 직접 사용
         */

        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();

        try {
            Team team = new Team("팀 A");
            em3.persist(team);

            User user = new User();
            user.setUserName("yunho");
            user.changeTeam(team);
            em3.persist(user);

            em3.flush();
            em3.clear();

            //엔티티를 파라미터 값으로 전달
            em3.createQuery("select u from User u where u = :user")
                    .setParameter("user", user)
                    .getResultList();

            em3.createQuery("select u from User u where u.team = :team")
                    .setParameter("team", team)
                    .getResultList();

            //식별자를 직접 전달
            em3.createQuery("select u from User u where u.id = :userId")
                    .setParameter("userId", 1L)
                    .getResultList();

            em3.createQuery("select u from User u where u.team.id = :teamId")
                    .setParameter("teamId", 1L)
                    .getResultList();

            //둘의 결과는 똑같다!

            tx3.commit();

        } catch (Exception e) {
            tx3.rollback();
            e.printStackTrace();

        } finally {
            em3.close();
        }

        System.out.println("=============================================");

        /**
         * Named 쿼리 - 정적 쿼리
         */

        EntityManager em4 = emf.createEntityManager();
        EntityTransaction tx4 = em4.getTransaction();
        tx4.begin();

        try {
            Team team = new Team("팀 A");
            em4.persist(team);

            User user = new User();
            user.setUserName("yunho");
            user.changeTeam(team);
            em4.persist(user);

            em4.flush();
            em4.clear();

            //User 엔티티에 임의의 쿼리 이름으로 NamedQuery 선언
            //User.findByUserName = "select u from User u where u.userName = :userName"
            em4.createNamedQuery("User.findByUserName")
                    .setParameter("userName", "yunho")
                    .getResultList();

            //JPARepository 인터페이스에서 메소드 위에 @Query(...) 선언하는 것도 네임드쿼리이다. (이걸 더 선호)

            tx4.commit();

        } catch (Exception e) {
            tx4.rollback();
            e.printStackTrace();

        } finally {
            em4.close();
        }

        System.out.println("=============================================");

        /**
         * 벌크 연산 : 영속성 컨텍스트를 무시하고 DB 에 직접 쿼리(flush)
         *  -> 벌크 연산을 먼저 수행 or 벌크 연산 수행 후 영속성 컨텍스트 초기화
         */

        EntityManager em5 = emf.createEntityManager();
        EntityTransaction tx5 = em5.getTransaction();
        tx5.begin();

        try {
            User user1 = new User();
            user1.setUserName("yunho1");
            user1.setAge(1);
            em5.persist(user1);

            User user2 = new User();
            user2.setUserName("yunho2");
            user2.setAge(2);
            em5.persist(user2);

            User user3 = new User();
            user3.setUserName("yunho3");
            user3.setAge(3);
            em5.persist(user3);

            //모든 user 의 나이를 20살로 업데이트 (flush 자동 호출)
            int resultCount = em5.createQuery("update User u set u.age = 20").executeUpdate();
            System.out.println("\nresultCount = " + resultCount);

            //flush 해도 영속성 컨텍스트가 아직 초기화되지 않았기 때문에 기존 데이터가 남아있음
            User findUser1 = em5.find(User.class, user1.getId());
            System.out.println("findUser1 age = " + findUser1.getAge() + "\n"); //1

            em5.clear(); //영속성 컨텍스트 초기화

            User findUser2 = em5.find(User.class, user1.getId()); //DB 에서 조회(select)
            System.out.println("\nfindUser2 age = " + findUser2.getAge()); //20

            tx5.commit();

        } catch (Exception e) {
            tx5.rollback();
            e.printStackTrace();

        } finally {
            em5.close();
        }

        emf.close();
    }
}
