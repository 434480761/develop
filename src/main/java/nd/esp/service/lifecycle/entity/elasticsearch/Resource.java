package nd.esp.service.lifecycle.entity.elasticsearch;

import org.junit.Assert;

public final class Resource {

	private String resourceType;
	private String identifier;

	public Resource(String resourceType, String identifier) {
		this.resourceType = resourceType;
		this.identifier = identifier;
	}

	public String getResourceType() {
		return resourceType;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Resource resource = (Resource) obj;
		if (resource.identifier != null) {
			if (!resource.identifier.equals(this.identifier)) {
				return false;
			}
		} else if (this.identifier != null) {
			return false;
		}

		if (resource.resourceType != null) {
			if (!resource.resourceType.equals(this.resourceType)) {
				return false;
			}
		} else if (this.resourceType != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 17 + 3 * (this.identifier == null ? 0 : identifier.hashCode())
				+ 5 * (this.resourceType == null ? 0 : resourceType.hashCode());
	}

	public static void main(String[] args) {
		Resource aResource = new Resource("questions", "123");
		Resource bResource = new Resource("questions", "123");
		Resource cResource = new Resource("assets", "123");
		Resource dResource = new Resource("assets", "456");

		Assert.assertTrue(aResource.equals(bResource));
		Assert.assertFalse(bResource.equals(cResource));
		Assert.assertFalse(bResource.equals(dResource));

		// hashcode
		Assert.assertTrue(aResource.hashCode() == bResource.hashCode());
		Assert.assertFalse(aResource.hashCode() == cResource.hashCode());
		Assert.assertFalse(aResource.hashCode() == dResource.hashCode());

		System.out.println("end success");
	}

}
