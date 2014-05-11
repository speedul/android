package simeonov.evgeni.model;

import com.google.android.gms.maps.model.LatLng;

public class PointOfInterest {
		
		private Integer id;
		private String title;
		private String details;
		private LatLng position;
		
		public PointOfInterest() {
			super();
		}
		
		public PointOfInterest(Integer id, String title, String details, LatLng coordinate) {
			super();
			this.id = id;
			this.title = title;
			this.details = details;
			this.position = coordinate;
		}
		
		public PointOfInterest(String title, String details, LatLng coordinate) {
			super();
			this.title = title;
			this.details = details;
			this.position = coordinate;
		}
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDetails() {
			return details;
		}
		public void setDetails(String details) {
			this.details = details;
		}
		public LatLng getPosition() {
			return position;
		}
		public void setPosition(LatLng coordinate) {
			this.position = coordinate;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
		
}
