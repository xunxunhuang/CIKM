package au.edu.rmit.csit.Quadtree;

import java.util.Hashtable;

public class NNEntry {
		public Node m_pEntry;
		public double m_minDist;
		public double m_maxDist;

		public NNEntry(Node e, double f) { 
			m_pEntry = e; 
			m_minDist = f; 
		}
}