package jpabasic.entityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaDetached {

    /**
     * 준영속 상태로 만드는 방법
     *
     *  1. em.detach(entity)
     *  2. em.clear()
     *  3. em.close()
     */

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member(1L, "yunho");

            //SELECT 쿼리만 나가고 UPDATE 쿼리는 나가지 않음
            Member findMember1 = em.find(Member.class, 1L);
            findMember1.setName("yunho1");
            em.clear();

            tx.commit();

        } catch (Exception e) {
            tx.rollback();

        } finally {
            em.close();
        }

        emf.close();
    }
}
