package au.edu.rmit.csit.index;

import java.util.Hashtable;

import au.edu.rmit.csit.index.QuadTreeP.PointRegionQuadTreeP.PointRegionQuadNode;
import au.edu.rmit.csit.index.QuadTreeP.QuadNode;

public class NNEntry {
		public QuadNode m_pEntry;
		public double m_minDist;
		public double m_maxDist;

		public NNEntry(QuadNode e, double f) { 
			m_pEntry = e; 
			m_minDist = f; 
		}
}