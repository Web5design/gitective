/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.gitective.tests;

import java.util.Arrays;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.AllCommitFilter;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.CommitListFilter;
import org.gitective.core.filter.commit.DiffFileSizeFilter;
import org.gitective.core.filter.commit.DiffLineSizeFilter;
import org.junit.Test;

/**
 * Unit tests of {@link DiffLineSizeFilter} and {@link DiffFileSizeFilter}
 */
public class DiffSizeTest extends GitTestCase {

	/**
	 * Test selecting commits where only one file differs
	 *
	 * @throws Exception
	 */
	@Test
	public void lineDiffSingleFile() throws Exception {
		RevCommit commit1 = add("file.txt", "a\nb\nc");
		RevCommit commit2 = add("file.txt", "a\nb2\nc");
		RevCommit commit3 = add("file.txt", "");
		CommitListFilter commits = new CommitListFilter();
		new CommitFinder(testRepo).setFilter(
				new AllCommitFilter(new AndCommitFilter(new DiffLineSizeFilter(
						3), commits))).find();
		assertTrue(commits.getCommits().contains(commit1));
		assertFalse(commits.getCommits().contains(commit2));
		assertTrue(commits.getCommits().contains(commit3));
	}

	/**
	 * Test selecting commits where multiple files differ
	 *
	 * @throws Exception
	 */
	@Test
	public void lineDiffMultipleFiles() throws Exception {
		RevCommit commit1 = add(Arrays.asList("file1.txt", "file2.txt"),
				Arrays.asList("a\n", "b\n"));
		RevCommit commit2 = add(Arrays.asList("file1.txt", "file2.txt"),
				Arrays.asList("a1\n", "b\nc\nd\n"));
		RevCommit commit3 = add(Arrays.asList("file1.txt", "file2.txt"),
				Arrays.asList("a1\n", "b\nc\n"));
		CommitListFilter commits = new CommitListFilter();
		new CommitFinder(testRepo).setFilter(
				new AllCommitFilter(new AndCommitFilter(new DiffLineSizeFilter(
						3), commits))).find();
		assertFalse(commits.getCommits().contains(commit1));
		assertTrue(commits.getCommits().contains(commit2));
		assertFalse(commits.getCommits().contains(commit3));
	}

	/**
	 * Test selecting commits multiple files are changed
	 *
	 * @throws Exception
	 */
	@Test
	public void fileDiffMultipleFiles() throws Exception {
		RevCommit commit1 = add(testRepo,
				Arrays.asList("file1.txt", "file2.txt"),
				Arrays.asList("a", "b"), "commit1");
		RevCommit commit2 = add(testRepo, Arrays.asList("file1.txt"),
				Arrays.asList("a1"), "commit1");
		RevCommit commit3 = add(testRepo,
				Arrays.asList("file1.txt", "file2.txt"),
				Arrays.asList("a2", "b1"), "commit3");
		CommitListFilter commits = new CommitListFilter();
		DiffFileSizeFilter diffFilter = new DiffFileSizeFilter(2);
		assertEquals(2, diffFilter.getTotal());
		new CommitFinder(testRepo).setFilter(
				new AllCommitFilter(new AndCommitFilter(diffFilter, commits)))
				.find();
		assertTrue(commits.getCommits().contains(commit1));
		assertFalse(commits.getCommits().contains(commit2));
		assertTrue(commits.getCommits().contains(commit3));
	}

	/**
	 * Test file with single non-rename revision
	 *
	 * @throws Exception
	 */
	@Test
	public void diffMovedFile() throws Exception {
		RevCommit commit1 = add("file.txt", "a\nb\nc");
		RevCommit commit2 = mv("file.txt", "file2.txt");
		CommitListFilter commits = new CommitListFilter();
		new CommitFinder(testRepo).setFilter(
				new AllCommitFilter(new AndCommitFilter(new DiffLineSizeFilter(
						true, 1), commits))).find();
		assertTrue(commits.getCommits().contains(commit1));
		assertFalse(commits.getCommits().contains(commit2));
	}

	/**
	 * Test of {@link DiffLineSizeFilter#clone()}
	 */
	@Test
	public void cloneFilter() {
		DiffLineSizeFilter filter = new DiffLineSizeFilter(10);
		RevFilter clone = filter.clone();
		assertNotNull(clone);
		assertNotSame(filter, clone);
		assertTrue(clone instanceof DiffLineSizeFilter);
		assertEquals(filter.getTotal(), ((DiffLineSizeFilter) clone).getTotal());
	}
}
