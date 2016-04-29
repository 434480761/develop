package nd.esp.service.lifecycle.repository.sdk.impl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nd.esp.service.lifecycle.repository.Education;
import nd.esp.service.lifecycle.repository.sdk.Contribute4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.CoursewareObjectRepository;
import nd.esp.service.lifecycle.repository.sdk.QuestionRepository;
import nd.esp.service.lifecycle.repository.sdk.ResCoverage4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceAnnotation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceCategory4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelation4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.ResourceStatistical4QuestionDBRepository;
import nd.esp.service.lifecycle.repository.sdk.TechInfo4QuestionDBRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @Description
 * @author Rainy(yang.lin)
 * @date 2015年5月15日 下午6:10:25
 * @version V1.0
 * @param <R>
 * @param <T>
 * @param <I>
 */
public class MyRepositoryFactoryBean<R extends JpaRepository<T, I>, T extends Education, I extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, I> {

	@Autowired
	@Qualifier(value = "defaultJdbcTemplate")
	JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier(value = "transactionTemplate")
	TransactionTemplate transactionTemplate;

	@Autowired
	@Qualifier(value = "questionJdbcTemplate")
	JdbcTemplate questionJdbcTemplate;

	@Autowired
	@Qualifier(value = "questionTransactionTemplate")
	TransactionTemplate questionTransactionTemplate;
	
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em2;
	
	@PersistenceContext(unitName="questionEntityManagerFactory")
	EntityManager questionEm;

	@Override
	@PersistenceContext()
	public void setEntityManager(EntityManager entityManager) {
		super.setEntityManager(entityManager);
	}

	/**
	 * Description
	 * 
	 * @param em
	 * @return
	 * @see org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean#createRepositoryFactory(javax.persistence.EntityManager)
	 */

	protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
		return new MyRepositoryFactory<T, I>(em,questionEm,jdbcTemplate,transactionTemplate);
	}

	/**
	 * A factory for creating MyRepository objects.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 */
	private static class MyRepositoryFactory<T extends Education, I extends Serializable>
			extends JpaRepositoryFactory {

		/** The em. */
		private final EntityManager em;
		
		private final EntityManager questionEm;

		private JdbcTemplate jdbcTemplate;

		private TransactionTemplate transactionTemplate;

		/**
		 * Instantiates a new my repository factory.
		 *
		 * @param em
		 *            the em
		 */
		public MyRepositoryFactory(EntityManager em,EntityManager questionEm, JdbcTemplate jdbcTemplate,
				TransactionTemplate transactionTemplate) {

			super(em);
			this.em = em;
			
			this.questionEm = questionEm;

			this.jdbcTemplate = jdbcTemplate;

			this.transactionTemplate = transactionTemplate;

		}

		/**
		 * Description
		 * 
		 * @param metadata
		 * @return
		 * @see org.springframework.data.jpa.repository.support.JpaRepositoryFactory#getTargetRepository(org.springframework.data.repository.core.RepositoryMetadata)
		 */

		@SuppressWarnings("unchecked")
		protected Object getTargetRepository(RepositoryMetadata metadata) {
			String repositoryName = metadata.getRepositoryInterface().toString();
			if(isQuestionDb(repositoryName)){
				return new ProxyRepositoryImpl<T, I>(
						(Class<T>) metadata.getDomainType(), questionEm, jdbcTemplate,
						transactionTemplate);
			}
			return new ProxyRepositoryImpl<T, I>(
					(Class<T>) metadata.getDomainType(), em, jdbcTemplate,
					transactionTemplate);
		}
		
		/**
		 * 判断是否是走习题库
		 * @param repositoryName
		 * @return
		 */
		private boolean isQuestionDb(String repositoryName){
			if (repositoryName.equals(QuestionRepository.class.toString())
					|| repositoryName.equals(CoursewareObjectRepository.class
							.toString())
					|| repositoryName
							.equals(TechInfo4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(ResCoverage4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(ResourceCategory4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(ResourceRelation4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(ResourceAnnotation4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(ResourceStatistical4QuestionDBRepository.class
									.toString())
					|| repositoryName
							.equals(Contribute4QuestionDBRepository.class
									.toString())) {
				return true;
			}
			return false;
		}
		
		/**
		 * Description
		 * 
		 * @param metadata
		 * @return
		 * @see org.springframework.data.jpa.repository.support.JpaRepositoryFactory#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
		 */

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return ProxyRepositoryImpl.class;
		}

		/**
		 * Gets the em.
		 *
		 * @return the em
		 */
		@SuppressWarnings("unused")
		public EntityManager getEm() {
			return em;
		}
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

}
