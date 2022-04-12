package jpabasic.jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class JpqlQuery {

    /**
     * JPQL
     *  1. 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
     *  2. SQL 을 추상화해서 특정 DB 의 SQL 에 의존 x
     *  3. JPQL 을 한마디로 정의하면 객체 지향 SQL
     */

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        /**
         * JPQL : 실무에선 JPQL 과 QueryDSL 이 90% 이상
         */
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        try {
            List<User> result = em1.createQuery(
                    "select u from User u where u.userName like '%kim'", User.class)
                    .getResultList();

            for (User user : result) {
                System.out.println("user = " + user);
            }

            tx1.commit();

        } catch (Exception e) {
            tx1.rollback();
            e.printStackTrace();

        } finally {
            em1.close();
        }

        System.out.println("=============================================");

        /**
         * 동적 쿼리(JPA Criteria) : 유지보수가 어렵다.. -> QueryDSL
         */
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            CriteriaBuilder cb = emf.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> u = query.from(User.class);

            CriteriaQuery<User> cq = query.select(u).where(cb.equal(u.get("userName"), "kim"));
            List<User> resultList = em2.createQuery(cq).getResultList();

            tx2.commit();

        } catch (Exception e) {
            tx2.rollback();
            e.printStackTrace();

        } finally {
            em2.close();
        }

        System.out.println("=============================================");

        /**
         * 네이티브 SQL : 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
         */
        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();

        try {
            Team team = new Team();
            team.setName("teamA");

            User user = new User();
            user.setUserName("yunho");
            user.setAge(12);
            user.changeTeam(team);

            em3.persist(team);
            em3.persist(user);
            em3.flush();
            em3.clear();

            em3.createNativeQuery("select USER_ID, userName, age from User")
                    .getResultList();

            tx3.commit();

        } catch (Exception e) {
            tx3.rollback();
            e.printStackTrace();

        } finally {
            em3.close();
        }

        emf.close();
    }
}
